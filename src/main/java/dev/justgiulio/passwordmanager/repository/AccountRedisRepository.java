package dev.justgiulio.passwordmanager.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.justgiulio.passwordmanager.model.Account;
import dev.justgiulio.passwordmanager.model.Credential;
import redis.clients.jedis.Jedis;

public class AccountRedisRepository implements AccountRepository {

	Jedis client;

	public AccountRedisRepository(Jedis client) {
		this.client = client;
	}

	@Override
	public List<Account> findAll() {
		List<String> keys;
		List<Account> accounts = new ArrayList<>();
		keys = client.keys("*").stream().collect(Collectors.toList());
		keys
			.stream()
			.map(key -> this.fromMapToAccounts(key,this.getMapFromKey(key)))
			.forEach(accounts::addAll);
		sortAccounts(accounts);
		return accounts;
	}

	@Override
	public List<Account> findByKey(String key) {
		List<Account> accounts;
		accounts = this.fromMapToAccounts(key,this.getMapFromKey(key));
		return accounts;
	}
	
	@Override
	public List<Account> findByUsername(String username) {
		List<Account> allAccounts = this.findAll();
		return allAccounts.stream().filter(account -> account.getCredential().getUsername().equals(username)).collect(Collectors.toList());
	}
	
	@Override
	public List<Account> findByPassword(String password) {
		List<Account> allAccounts = this.findAll();
		return allAccounts.stream().filter(account -> account.getCredential().getPassword().equals(password)).collect(Collectors.toList());
	}
	
	@Override
	public String save(Account accountToSave) {
		Map<String, String> mapToSave = new HashMap<>();
		mapToSave.put(accountToSave.getCredential().getUsername(), accountToSave.getCredential().getPassword());
		return this.client.hmset(accountToSave.getSite(), mapToSave);
	}
	
	@Override
	public void delete(Account account) {
		this.client.hdel(account.getSite(), account.getCredential().getUsername());
	}
	
	
	
	
	/** Private Methods */

	private List<Account> fromMapToAccounts(String key, Map<String, String> jedisSavedMap) {
		List<String> savedKeys;
		List<Account> savedAccounts;
		List<Credential> savedCredential;
		savedKeys = jedisSavedMap.keySet().stream().collect(Collectors.toList());
		savedCredential = savedKeys.stream().map(keyMap -> new Credential(keyMap,jedisSavedMap.get(keyMap)))
				.collect(Collectors.toList());
		savedAccounts = savedCredential.stream().map(credential -> new Account(key, credential))
				.collect(Collectors.toList());
		return savedAccounts;
	}
	
	

	private Map<String,String> getMapFromKey(String key){
		return this.client.hgetAll(key);
	}

	
	private void sortAccounts(List<Account> accounts) {
		Comparator<Account> bySite = (Account tmpAccount1, Account tmpAccount2)->tmpAccount1.getSite().compareTo(tmpAccount2.getSite());	
		accounts.sort(bySite);
	}

	

	
	

}
