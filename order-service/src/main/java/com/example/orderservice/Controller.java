package com.example.orderservice;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.appinfo.ApplicationInfoManager;

@RestController
public class Controller {

	@Autowired
	private ApplicationInfoManager applicationInfoManager;

	@GetMapping("/")
	public String get(HttpServletRequest request) {
		return "SERVICE: " + applicationInfoManager.getInfo().getAppName() + "\nPORT: "
				+ applicationInfoManager.getInfo().getPort();
	}

}
