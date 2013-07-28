package sybyla.jaxb;

public enum ApiErrors {
    
    // These values must match up with what's in the syb-api.xsd "error_type" list.
    InternalError,
    IllegalArgumentError,
    UnauthorizedAccessError,
    AboveQuotaError
}
