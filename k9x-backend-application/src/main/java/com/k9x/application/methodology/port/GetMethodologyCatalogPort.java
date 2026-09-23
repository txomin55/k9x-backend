package com.k9x.application.methodology.port;

import com.k9x.application.methodology.use_case.dto.MethodologyCatalogDTO;

/**
 * The names the methodology pages print next to the domain figures: which OBDX configurations are current, and
 * what configurations, federations, categories and qualifications are called. The figures themselves never come
 * from here — they are computed from the domain rules.
 */
public interface GetMethodologyCatalogPort {

    MethodologyCatalogDTO getCatalog();
}
