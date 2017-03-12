package com.lh.kafka.stream.serdes;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class SerdesFactory {

	public static <T> Serde<T> serdFrom(Class<T> pojoClass) {
		return Serdes.serdeFrom(new GenericSerializer<T>(pojoClass), new GenericDeserializer<T>(pojoClass));
	}

}
