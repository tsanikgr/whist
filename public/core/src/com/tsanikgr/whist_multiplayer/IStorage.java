package com.tsanikgr.whist_multiplayer;

public interface IStorage {
	String load(String name);
	void save(String name, String value);
	boolean delete(String name);
}
