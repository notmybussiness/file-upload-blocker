package com.madrascheck.extensionblocker;

import org.springframework.boot.SpringApplication;

public class TestExtensionBlockerApplication {

	public static void main(String[] args) {
		SpringApplication.from(ExtensionBlockerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
