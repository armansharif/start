package com.project.common.core.error;

/**
 * Thrown when a requested resource does not exist or is not visible to the caller.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(CommonErrorCode.RESOURCE_NOT_FOUND, resourceName, id);
    }

    public ResourceNotFoundException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode, messageArgs);
    }
}
