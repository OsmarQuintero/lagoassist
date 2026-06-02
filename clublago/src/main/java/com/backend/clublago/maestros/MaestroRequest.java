package com.backend.clublago.maestros;

import java.util.List;

public record MaestroRequest(
	String name,
	String phone,
	String email,
	String username,
	String password,
	boolean active,
	List<Long> disciplineIds
) {
}
