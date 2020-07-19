package dev.justgiulio.passwordmanager.view.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.fppt.jedismock.RedisServer;

import dev.justgiulio.passwordmanager.controller.AccountController;
import dev.justgiulio.passwordmanager.generator.Generator;
import dev.justgiulio.passwordmanager.generator.SecureRandomGenerator;
import dev.justgiulio.passwordmanager.model.Account;
import dev.justgiulio.passwordmanager.model.Credential;
import dev.justgiulio.passwordmanager.repository.AccountRedisRepository;
import redis.clients.jedis.Jedis;

@RunWith(GUITestRunner.class)
public class AccountSwingViewIT extends AssertJSwingJUnitTestCase {

	private FrameFixture window;
	private AccountSwingView accountSwingView;
	private static Jedis jedis;
	private static RedisServer server = null;
	private static Generator passwordGenerator;
	private AccountRedisRepository accountRedisRepository;
	private AccountController accountController;

	@BeforeClass
	public static void beforeClass() throws IOException {
		server = RedisServer.newRedisServer(); // bind to a random port
		server.start();
		jedis = new Jedis(server.getHost(), server.getBindPort());
	}

	@Before
	public void onSetUp() {
		accountRedisRepository = new AccountRedisRepository(jedis);
		passwordGenerator = new SecureRandomGenerator();

		GuiActionRunner.execute(() -> {
			accountSwingView = new AccountSwingView();
			accountController = new AccountController(accountSwingView, accountRedisRepository, passwordGenerator);
			accountSwingView.setAccountController(accountController);
			return accountSwingView;
		});

		window = new FrameFixture(robot(), accountSwingView);
		window.show(); // shows the frame to test
	}

	@After
	public void after() {
		jedis.flushAll();
	}

	@Test
	@GUITest
	public void testAccountIsModifiedPassowrd() {
		Account accountToSave = new Account("github.com", new Credential("remeic", "remepassword"));
		accountRedisRepository.save(accountToSave);
		window.tabbedPane("tabbedPanel").selectTab(1);
		accountController.findAllAccounts();
		window.table("tableDisplayedAccounts").selectRows(0);
		window.textBox("textFieldUpdateCell").enterText("newPassword");
		window.button("buttonModifyPassword").click();
		accountController.findAllAccounts();
		List<Account> accountList = getAccountsList(window.table("tableDisplayedAccounts").contents());
		assertThat(accountList).containsExactly(new Account("github.com", new Credential("remeic", "newPassword")));
	}

	@Test
	@GUITest
	public void testAccountIsModifiedUsername() {
		Account accountToSave = new Account("github.com", new Credential("remeic", "remepassword"));
		accountRedisRepository.save(accountToSave);
		window.tabbedPane("tabbedPanel").selectTab(1);
		accountController.findAllAccounts();
		window.table("tableDisplayedAccounts").selectRows(0);
		window.textBox("textFieldUpdateCell").enterText("newUsername");
		window.button("buttonModifyUsername").click();
		accountController.findAllAccounts();
		List<Account> accountList = getAccountsList(window.table("tableDisplayedAccounts").contents());
		assertThat(accountList).containsExactly(new Account("github.com", new Credential("newUsername", "remepassword")));
	}

	@Test
	@GUITest
	public void testAccountIsDeleted() {
		Account accountToSave = new Account("github.com", new Credential("remeic", "remepassword"));
		accountRedisRepository.save(accountToSave);
		window.tabbedPane("tabbedPanel").selectTab(1);
		accountController.findAllAccounts();
		window.table("tableDisplayedAccounts").selectRows(0);
		window.button("buttonDeleteAccount").click();
		accountController.findAllAccounts();
		List<Account> accountList = getAccountsList(window.table("tableDisplayedAccounts").contents());
		assertThat(accountList).isEmpty();
	}
	
	

	@Test
	@GUITest
	public void testSaveAccountShowedInFindAll() {
		Account firstAccount = new Account("github.com", new Credential("remeic", "remepassword"));
		accountController.saveAccount(firstAccount);
		window.tabbedPane("tabbedPanel").selectTab(1);
		accountController.findAllAccounts();
		List<Account> accountList = getAccountsList(window.table("tableDisplayedAccounts").contents());
		assertThat(accountList).containsExactly(firstAccount);
	}

	@Test
	@GUITest
	public void testFindAllAccounts() {
		window.tabbedPane("tabbedPanel").selectTab(1);
		Account firstAccount = new Account("github.com", new Credential("remeic", "remepassword"));
		Account secondAccount = new Account("gitlab.com", new Credential("giulio", "giuliopassword"));
		accountRedisRepository.save(firstAccount);
		accountRedisRepository.save(secondAccount);
		window.tabbedPane("tabbedPanel").selectTab(1);
		accountController.findAllAccounts();
		List<Account> accountList = getAccountsList(window.table("tableDisplayedAccounts").contents());
		assertThat(accountList).containsExactly(firstAccount, secondAccount);
	}

	@Test
	@GUITest
	public void testFindByKey() {
		window.tabbedPane("tabbedPanel").selectTab(1);
		String site = "github.com";
		Account firstAccount = new Account(site, new Credential("remeic", "remepassword"));
		Account secondAccount = new Account("gitlab.com", new Credential("giulio", "giuliopassword"));
		Account thirdAccount = new Account(site, new Credential("remegiulio", "remepassword"));
		accountRedisRepository.save(firstAccount);
		accountRedisRepository.save(secondAccount);
		accountRedisRepository.save(thirdAccount);
		window.tabbedPane("tabbedPanel").selectTab(1);
		accountController.findAccountsByKey(site);
		List<Account> accountList = getAccountsList(window.table("tableDisplayedAccounts").contents());
		assertThat(accountList).containsExactly(firstAccount, thirdAccount);
	}

	@Test
	@GUITest
	public void testFindByUsername() {
		window.tabbedPane("tabbedPanel").selectTab(1);
		String username = "remeic";
		Account firstAccount = new Account("github.com", new Credential(username, "remepassword"));
		Account secondAccount = new Account("gitlab.com", new Credential("giulio", "giuliopassword"));
		Account thirdAccount = new Account("gitlab.com", new Credential(username, "remepassword"));
		accountRedisRepository.save(firstAccount);
		accountRedisRepository.save(secondAccount);
		accountRedisRepository.save(thirdAccount);
		accountController.findAccountsByUsername(username);
		List<Account> accountList = getAccountsList(window.table("tableDisplayedAccounts").contents());
		assertThat(accountList).containsExactly(firstAccount,thirdAccount);
	}

	private List<Account> getAccountsList(String[][] tableContent) {
		List<Account> accounts = new ArrayList<Account>();
		for (int i = 0; i < tableContent.length; i++) {
			accounts.add(new Account(tableContent[i][0], new Credential(tableContent[i][1], tableContent[i][2])));
		}
		return accounts;
	}

}
