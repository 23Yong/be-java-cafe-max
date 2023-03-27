package kr.codesqaud.cafe.domain.user;

import kr.codesqaud.cafe.controller.dto.req.JoinRequest;

public class User {

	private String userId;
	private String password;
	private String name;
	private String email;

	private User(String userId, String password, String name, String email) {
		this.userId = userId;
		this.password = password;
		this.name = name;
		this.email = email;
	}

	public static User from(final JoinRequest joinRequest) {
		return new User(joinRequest.getUserId(),
			joinRequest.getPassword(),
			joinRequest.getName(),
			joinRequest.getEmail());
	}

	public String getUserId() {
		return userId;
	}
}