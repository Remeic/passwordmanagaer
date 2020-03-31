package dev.justgiulio.passwordmanager.repository;


import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.ListAssert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.fppt.jedismock.RedisServer;

import dev.justgiulio.passwordmanager.model.Account;
import dev.justgiulio.passwordmanager.model.Credential;
import redis.clients.jedis.Jedis;

public class AccountRepositoryRedistTest {
	
	private static AccountRedisRepository accountRedisRepository;
	private static Jedis jedis;
	private static RedisServer server = null;

	@BeforeClass
	public static void beforeClass()   throws IOException {
		  server = RedisServer.newRedisServer();  // bind to a random port
		  server.start();
		  jedis = new Jedis(server.getHost(), server.getBindPort());
		  accountRedisRepository = new AccountRedisRepository(jedis);
	}
	
	@After
	public void after() {
		jedis.flushAll();
	}
	
	@AfterClass
	public static void afterClass() {
	  server.stop();
	  server = null;
	}
	
	@Test
	public void testFindAllWhenNotFound() {
		ListAssert<Account> assertThat = assertThat(accountRedisRepository.findAll());
		assertThat.isEmpty();
	}

	@Test
	public void testFindAllWhenDatabaseIsNotEmpty() {
		Account accountToSave = new Account("github.com", new Credential("username","password"));
		addAccountToRedisDatabase(accountToSave);
		ListAssert<Account> assertThat = assertThat(accountRedisRepository.findAll());
		assertThat.containsExactly(accountToSave);
	}
	
	/**
	 * Utility Method for Add Account to Database
	 * @param account Target Account to save on Redis Database
	 * @return Result of save operation
	 */
	public String addAccountToRedisDatabase(Account account) {
		Map<String, String> tmpMap = new HashMap<String, String>();
		Credential tmpCredential = account.getCredential();
		tmpMap.put(tmpCredential.getUsername(), tmpCredential.getPassword() );
		String result =  jedis.hmset(account.getSite(), tmpMap);
		return result;
	}
}
