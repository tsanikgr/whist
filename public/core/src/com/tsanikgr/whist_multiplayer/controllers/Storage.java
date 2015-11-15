package com.tsanikgr.whist_multiplayer.controllers;

import com.tsanikgr.whist_multiplayer.Config;
import com.tsanikgr.whist_multiplayer.IPlatformStorage;
import com.tsanikgr.whist_multiplayer.util.ICrypto;
import com.tsanikgr.whist_multiplayer.util.Cryptography;
import com.tsanikgr.whist_multiplayer.IStorage;

public class Storage extends Controller implements IStorage{

	private IPlatformStorage storage;
	private ICrypto encryptor;

	public Storage(IPlatformStorage storage) {
		this.storage = storage;
		if (storage == null) createDumbStorage();
	}

	@Override
	public void init() {
		if (storage != null && Config.ENABLE_CRYPTOGRAPHY) {
			byte[] salt = getDeviceSalt();
			if (salt == null) salt = Config.getDefaultSalt();
			encryptor = new Cryptography(Config.STORAGE_PASSWORD, salt, Config.getIvSeparator());
		}

		if (encryptor == null || !encryptor.isReady()) {
			log.w().append("Storage data not encrypted").print();
			encryptor = new ICrypto() {
				@Override
				public String encrypt(String message) {
					return message;
				}
				@Override
				public String decrypt(String message) {
					return message;
				}
				@Override
				public void changePassword(String newPassword) {
				}
				@Override
				public boolean isReady() {
					return true;
				}
			};
		}
	}

	@Override
	public void disposeController() {
		encryptor = null;
	}

	@Override
	public String load(String name) {
		String s = storage.read(name);
		return s == null ? null : encryptor.decrypt(s);
	}

	@Override
	public void save(String name, String value) {
		String encrypted = encryptor.encrypt(value);
		if (encrypted != null) storage.write(name, encrypted);
	}

	@Override
	public boolean delete(String name) {
		return storage.delete(name);
	}

	private byte[] getDeviceSalt() {
		return storage.getDeviceSpecificSalt();
	}

	public void registerFileChangeListener(String file, IPlatformStorage.OnFileChangedListener listener) {
		storage.registerFileChangeListener(file, listener);
	}

	public void unregisterFileChangeListener(String file, IPlatformStorage.OnFileChangedListener listener) {
		storage.registerFileChangeListener(file, listener);
	}

	private void createDumbStorage(){
		storage = new IPlatformStorage() {
			@Override
			public byte[] getDeviceSpecificSalt() {
				return Config.getDefaultSalt();
			}
			@Override
			public void write(String key, String value) {
			}
			@Override
			public String read(String key) {
				return null;
			}
			@Override
			public boolean delete(String key) {
				return false;
			}
			@Override
			public void registerFileChangeListener(String preferences, OnFileChangedListener listener) {
			}
			@Override
			public void unregisterFileChangeListener(String preferences) {

			}
		};
	}
}
