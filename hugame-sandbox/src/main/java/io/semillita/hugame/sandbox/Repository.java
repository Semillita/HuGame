package io.semillita.hugame.sandbox;

import dev.hugame.inject.Global;
import dev.hugame.inject.Init;
import dev.hugame.inject.Inject;

@Global
public class Repository {

	@Inject
	private Service service;
	
	@Init
	public void initialize() {
		System.out.println("Initialized repository");
	}
	
	public void something() {
		System.out.println("Repository something: " + service);
	}
	
}
