package io.semillita.hugame.sandbox;

import dev.hugame.inject.Global;
import dev.hugame.inject.Init;
import dev.hugame.inject.Inject;

@Global
public class Service {

	@Inject
	private Repository repository;
	
	@Init
	public void initialize() {
		System.out.println("Initialize service");
	}
	
	public void something() {
		System.out.println("Service something: " + repository);
	}
	
}
