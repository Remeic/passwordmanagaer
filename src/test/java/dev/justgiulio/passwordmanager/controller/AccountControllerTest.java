
package dev.justgiulio.passwordmanager.controller;

import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dev.justgiulio.passwordmanager.generator.Generator;
import dev.justgiulio.passwordmanager.model.Account;
import dev.justgiulio.passwordmanager.model.Credential;
import dev.justgiulio.passwordmanager.repository.AccountRepository;
import dev.justgiulio.passwordmanager.view.AccountView;

public class AccountControllerTest {

	@Mock
	private AccountRepository accountRepository;
	
	@Mock
	private AccountView accountView;
	
	@Mock
	private Generator passwordGenerator;
	
	@InjectMocks
	AccountController controller;
	
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void findAllAccountTest() {
		List<Account> accounts = Arrays.asList(new Account("github.com", new Credential("giulio","passgiulio")));
		when(accountRepository.findAll()).thenReturn(accounts);
		controller.findAllAccounts();
		verify(accountView).showAccounts(accounts);
	}
	
	@Test
	public void findAccountsBySiteTest() {
		String site = "github";
		List<Account> accounts = Arrays.asList(new Account("github.com", new Credential("giulio","passgiulio")));
		when(accountRepository.findByKey(site)).thenReturn(accounts);
		controller.findAccountsByKey(site);
		verify(accountView).showAccounts(accounts);
	}
	
	@Test
	public void findAccountsByUsernameTest() {
		String username = "giulio";
		List<Account> accounts = Arrays.asList(new Account("github.com", new Credential("giulio","passgiulio")));
		when(accountRepository.findByUsername(username)).thenReturn(accounts);
		controller.findAccountsByUsername(username);
		verify(accountView).showAccounts(accounts);
	}
	
	@Test
	public void findAccountsByPasswordTest() {
		String password = "passgiulio";
		List<Account> accounts = Arrays.asList(new Account("github.com", new Credential("giulio","passgiulio")));
		when(accountRepository.findByPassword(password)).thenReturn(accounts);
		controller.findAccountsByPassword(password);
		verify(accountView).showAccounts(accounts);
	}
	
	@Test
	public void saveAccountWhenIsDatabaseIsEmptyTest() {
		Account accountToSave = new Account("github.com", new Credential("giulio","passgiulio"));
		InOrder inOrder = inOrder(accountRepository,accountView);
		controller.saveAccount(accountToSave);
		inOrder.verify(accountRepository).save(accountToSave);
		inOrder.verify(accountView).accountIsAdded();
	}
	
	@Test
	public void saveAccountAlreadyExistsTest() {
		String site = "github.com";
		Account accountToSave = new Account("github.com", new Credential("giulio","passgiulio"));
		Account accountAlreadySaved = new Account("github.com", new Credential("giulio","passgiulio"));
		when(accountRepository.findByKey(site)).thenReturn(Arrays.asList(accountToSave, new Account("github.com",new Credential("remeic","passremeic"))));
		controller.saveAccount(accountToSave);
		verify(accountView).showError("Already existing: ", accountAlreadySaved);
		verifyNoMoreInteractions(ignoreStubs(accountRepository));
	}
	
	@Test
	public void saveAccountWhenDatabaseIsNotEmptyTest() {
		String site = "github.com";
		Account accountToSave = new Account("github.com", new Credential("giulio","passgitlab"));
		InOrder inOrder = inOrder(accountRepository,accountView);
		when(accountRepository.findByKey(site)).thenReturn(Arrays.asList(new Account("github.com",new Credential("remeic","passremeic"))));
		controller.saveAccount(accountToSave);
		inOrder.verify(accountRepository).save(accountToSave);
		inOrder.verify(accountView).accountIsAdded();
	}
	
	@Test
	public void modifyAccountPasswordTest() {
		String site = "github.com";
		String newCredentialPassword = "passMoreSecure123";
		Account alreadySavedAccount = new Account("github.com", new Credential("giulio","passgiulio"));
		InOrder inOrder = inOrder(accountRepository,accountView);
		when(accountRepository.findByKey(site)).thenReturn(Arrays.asList(alreadySavedAccount));
		controller.modifyPassword(alreadySavedAccount, newCredentialPassword);
		inOrder.verify(accountRepository).save(new Account("github.com", new Credential("giulio",newCredentialPassword)));
		inOrder.verify(accountView).accountIsModified();
	}
	
	@Test
	public void modifyAccountUsernameTest() {
		String site = "github.com";
		String newCredentialUsername = "remeic";
		Account alreadySavedAccount = new Account("github.com",new Credential("giulio","passgiulio"));
		InOrder inOrder = inOrder(accountRepository,accountView);
		when(accountRepository.findByKey(site)).thenReturn(Arrays.asList(alreadySavedAccount));
		controller.modifyUsername(alreadySavedAccount,newCredentialUsername);
		inOrder.verify(accountRepository).delete(alreadySavedAccount);
		inOrder.verify(accountRepository).save( new Account("github.com",new Credential(newCredentialUsername,"passgiulio")));
		inOrder.verify(accountView).accountIsModified();
	}
	
	@Test
	public void modifyAccountPasswordWhenAccountDoesntExistsNotOperationPerfomedTest() {
		String site = "github.com";
		String newCredentialPassword = "passMoreSecure123";
		when(accountRepository.findByKey(site)).thenReturn(Arrays.asList(new Account("github.com", new Credential("remegiulio","remepassword"))));
		controller.modifyPassword(new Account("github.com", new Credential("giulio","passgiulio")), newCredentialPassword);
		verify(accountView).showAccountRelatedError("Can't find any account for selected site with specified password");
		verifyNoMoreInteractions(ignoreStubs(accountRepository));
	}
	
	@Test
	public void modifyAccountUsernameWhenAccountDoesntExistsNotOperationPerfomedTest() {
		String site = "github.com";
		String newCredentialUsername = "remeic";
		when(accountRepository.findByKey(site)).thenReturn(Arrays.asList(new Account("github.com", new Credential("remegiulio","remepassword"))));
		controller.modifyUsername(new Account("github.com", new Credential("giulio","passgiulio")), newCredentialUsername);
		verify(accountView).showAccountRelatedError("Can't find any account for selected site with specified username");
		verifyNoMoreInteractions(ignoreStubs(accountRepository));
	}
	
	@Test
	public void deleteAccountTest() {
		String site = "github.com";
		Account accountToDelete = new Account("github.com", new Credential("remegiulio","remepassword"));
		InOrder inOrder = inOrder(accountRepository,accountView);
		when(accountRepository.findByKey(site)).thenReturn(Arrays.asList(accountToDelete));
		controller.delete(accountToDelete);
		inOrder.verify(accountRepository).delete(accountToDelete);
		inOrder.verify(accountView).accountIsDeleted();
	}
	
	@Test
	public void deleteAccountNotExistsNoPerformOperationTest() {
		String site = "github.com";
		Account accountToDelete = new Account("github.com", new Credential("remegiulio","remepassword"));
		when(accountRepository.findByKey(site)).thenReturn(Arrays.asList(accountToDelete));
		controller.delete( new Account("github.com", new Credential("giulio","remepassword")));
		verify(accountView).showAccountRelatedError("Can't find any account for selected site");
		verifyNoMoreInteractions(ignoreStubs(accountRepository));
	}
	

	@Test
	public void testGeneratePasswordLowStrengthCallPasswordIsGeneratedOnView() {
		String generatedPassword = "abcd";
		when(passwordGenerator.generate(4,0)).thenReturn(generatedPassword);
		controller.generatePassword(4,"STRENGHT_PASSWORD_LOW");
		verify(passwordGenerator).generate(4,0);
		verify(accountView).passwordIsGenereated(generatedPassword);
	}
	
	
	@Test
	public void testGeneratePasswordMediumStrengthCallPasswordIsGeneratedOnView() {
		String generatedPassword = "ab96";
		when(passwordGenerator.generate(4,1)).thenReturn(generatedPassword);
		controller.generatePassword(4,"STRENGHT_PASSWORD_MEDIUM");
		verify(passwordGenerator).generate(4,1);
		verify(accountView).passwordIsGenereated(generatedPassword);
	}
	
	@Test
	public void testGeneratePasswordHighStrengthCallPasswordIsGeneratedOnView() {
		String generatedPassword = "a!96";
		when(passwordGenerator.generate(4,2)).thenReturn(generatedPassword);
		controller.generatePassword(4,"STRENGHT_PASSWORD_HIGH");
		verify(passwordGenerator).generate(4,2);
		verify(accountView).passwordIsGenereated(generatedPassword);
	}
	
	
}
