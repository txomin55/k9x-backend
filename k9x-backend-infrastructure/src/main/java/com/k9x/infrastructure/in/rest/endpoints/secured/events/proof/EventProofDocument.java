package com.k9x.infrastructure.in.rest.endpoints.secured.events.proof;

/** A rendered proof: the bytes to send and the name the browser saves them under. */
public record EventProofDocument(byte[] content, String fileName) {
}
