package com.lh.kafka.stream;

import com.lh.kafka.model.Item;
import com.lh.kafka.model.Order;
import com.lh.kafka.model.User;
import com.lh.kafka.stream.serdes.SerdesFactory;
import com.lh.kafka.stream.timeextractor.OrderTimestampExtractor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.io.IOException;
import java.util.Properties;

public class PurchaseAnalysis {

    public static void main(String[] args) throws IOException, InterruptedException {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-purchase-analysis");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.118.149:9092");
        props.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "192.168.118.149:2181/kafka");
        props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, OrderTimestampExtractor.class);
        KStreamBuilder streamBuilder = new KStreamBuilder();
        KStream<String, Order> orderStream = streamBuilder.stream(Serdes.String(), SerdesFactory.serdFrom(Order.class), "orders");
        KTable<String, User> userTable = streamBuilder.table(Serdes.String(), SerdesFactory.serdFrom(User.class), "users", "users-state-store");
        KTable<String, Item> itemTable = streamBuilder.table(Serdes.String(), SerdesFactory.serdFrom(Item.class), "items", "items-state-store");
        orderStream
                .leftJoin(userTable, (Order order, User user) -> OrderUser.fromOrderUser(order, user), Serdes.String(), SerdesFactory.serdFrom(Order.class))
                .filter((String userName, OrderUser orderUser) -> orderUser.userAddress != null)
                .map((String userName, OrderUser orderUser) -> new KeyValue<String, OrderUser>(orderUser.itemName, orderUser))
                .through(Serdes.String(), SerdesFactory.serdFrom(OrderUser.class), (String key, OrderUser orderUser, int numPartitions) -> (orderUser.getItemName().hashCode() & 0x7FFFFFFF) % numPartitions, "orderuser-repartition-by-item")
                .leftJoin(itemTable, (OrderUser orderUser, Item item) -> OrderUserItem.fromOrderUser(orderUser, item), Serdes.String(), SerdesFactory.serdFrom(OrderUser.class))
                .filter((String item, OrderUserItem orderUserItem) -> (orderUserItem.age >= 18 && orderUserItem.age <= 35))
                .map((String item, OrderUserItem orderUserItem) -> KeyValue.<NameCategory, QuantityPrice>pair(new NameCategory(orderUserItem.userName, orderUserItem.itemName, orderUserItem.itemCategory), QuantityPrice.fromQuantityPrice(orderUserItem.quantity, orderUserItem.itemPrice)))
                .groupByKey(SerdesFactory.serdFrom(NameCategory.class), SerdesFactory.serdFrom(QuantityPrice.class))
                .reduce((QuantityPrice v1, QuantityPrice v2) -> QuantityPrice.fromQuantityPrice(v1.getQuantity() + v2.getQuantity(), v1.getPrice()), TimeWindows.of(2000).advanceBy(1000), "gender-amount-state-store").toStream()
                .map((Windowed<NameCategory> window, QuantityPrice value) -> {
                    return new KeyValue<String, String>(String.format("start=%d, end=%d, key=%s\n", window.window().start(), window.window().end(), window.key().toString()), String.valueOf(value));
                }).to(Serdes.String(), Serdes.String(), "gender-amount");
        KafkaStreams kafkaStreams = new KafkaStreams(streamBuilder, props);
        kafkaStreams.cleanUp();
        kafkaStreams.start();

        System.in.read();
        kafkaStreams.close();
        kafkaStreams.cleanUp();
    }

    public static class OrderUser {
        private String userName;
        private String itemName;
        private long transactionDate;
        private int quantity;
        private String userAddress;
        private String gender;
        private int age;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public long getTransactionDate() {
            return transactionDate;
        }

        public void setTransactionDate(long transactionDate) {
            this.transactionDate = transactionDate;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getUserAddress() {
            return userAddress;
        }

        public void setUserAddress(String userAddress) {
            this.userAddress = userAddress;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public static OrderUser fromOrder(Order order) {
            OrderUser orderUser = new OrderUser();
            if (order == null) {
                return orderUser;
            }
            orderUser.userName = order.getUserName();
            orderUser.itemName = order.getItemName();
            orderUser.transactionDate = order.getTransactionDate();
            orderUser.quantity = order.getQuantity();
            return orderUser;
        }

        public static OrderUser fromOrderUser(Order order, User user) {
            OrderUser orderUser = fromOrder(order);
            if (user == null) {
                return orderUser;
            }
            orderUser.gender = user.getGender();
            orderUser.age = user.getAge();
            orderUser.userAddress = user.getAddress();
            return orderUser;
        }
    }

    public static class OrderUserItem {
        private String userName;
        private String itemName;
        private long transactionDate;
        private int quantity;
        private String userAddress;
        private String gender;
        private int age;
        private String itemAddress;
        private String itemCategory;
        private double itemPrice;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public long getTransactionDate() {
            return transactionDate;
        }

        public void setTransactionDate(long transactionDate) {
            this.transactionDate = transactionDate;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getUserAddress() {
            return userAddress;
        }

        public void setUserAddress(String userAddress) {
            this.userAddress = userAddress;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getItemAddress() {
            return itemAddress;
        }

        public void setItemAddress(String itemAddress) {
            this.itemAddress = itemAddress;
        }

        public String getItemCategory() {
            return itemCategory;
        }

        public void setItemCategory(String itemCategory) {
            this.itemCategory = itemCategory;
        }

        public double getItemPrice() {
            return itemPrice;
        }

        public void setItemPrice(double itemPrice) {
            this.itemPrice = itemPrice;
        }

        public static OrderUserItem fromOrderUser(OrderUser orderUser) {
            OrderUserItem orderUserItem = new OrderUserItem();
            if (orderUser == null) {
                return orderUserItem;
            }
            orderUserItem.userName = orderUser.userName;
            orderUserItem.itemName = orderUser.itemName;
            orderUserItem.transactionDate = orderUser.transactionDate;
            orderUserItem.quantity = orderUser.quantity;
            orderUserItem.userAddress = orderUser.userAddress;
            orderUserItem.gender = orderUser.gender;
            orderUserItem.age = orderUser.age;
            return orderUserItem;
        }

        public static OrderUserItem fromOrderUser(OrderUser orderUser, Item item) {
            OrderUserItem orderUserItem = fromOrderUser(orderUser);
            if (item == null) {
                return orderUserItem;
            }
            orderUserItem.itemAddress = item.getAddress();
            orderUserItem.itemCategory = item.getCategory();
            orderUserItem.itemPrice = item.getPrice();
            return orderUserItem;
        }
    }
    public static class NameCategory {
        private String userNname;

        private String itemName;

        private String category;

        public NameCategory() {
        }

        public NameCategory(String userNname, String itemName, String category) {
            this.userNname = userNname;
            this.itemName = itemName;
            this.category = category;
        }

        public String getUserNname() {
            return userNname;
        }

        public void setUserNname(String userNname) {
            this.userNname = userNname;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        @Override
        public String toString() {
            return userNname + " " + itemName + " " +  category;
        }
    }

    public static  class QuantityPrice {

        private int quantity;

        private double price;

        private double total;

        public QuantityPrice() {
        }

        public QuantityPrice(int quantity, double price) {
            this.quantity = quantity;
            this.price = price;
        }

        public static QuantityPrice fromQuantityPrice(int quantity, Double itemPrice) {
            QuantityPrice quantityPrice = new QuantityPrice();
            quantityPrice.setQuantity(quantity);
            quantityPrice.setPrice(itemPrice);
            quantityPrice.setTotal((double) quantity * itemPrice);
            return quantityPrice;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
            this.total = total;
        }

        @Override
        public String toString() {
            return quantity + " " + price + " " + total;
        }

    }

}
