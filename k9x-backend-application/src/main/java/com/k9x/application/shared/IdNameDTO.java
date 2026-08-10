package com.k9x.application.shared;

/**
 * A reference pair: an identifier and its display name. Shared so features stop declaring their own copy of
 * the same two fields.
 */
public record IdNameDTO(String id, String name) {
}
