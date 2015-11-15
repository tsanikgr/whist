package com.tsanikgr.whist_multiplayer.models;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.tsanikgr.whist_multiplayer.util.Log;

public abstract class JsonSerialisable<T> {

	transient final Log log = new Log(this);
	private transient final Class<T> typeParameterClass;

	JsonSerialisable(Class<T> typeParameterClass){
		this.typeParameterClass = typeParameterClass;
	}

	public String serialise() {
		return new Json().toJson(this, typeParameterClass);
	}

	public static <T> T deserialise(String jsonStr, Class<T> typeParameterClass) {
		if (jsonStr == null) return null;
		try {
			return new Json().fromJson(typeParameterClass, jsonStr);
		} catch (SerializationException e) {
			new Log(null).e(e).append("Could not serialise data of ").append(typeParameterClass.getSimpleName()).append(" object").print();
			return null;
		}
	}
}
