package com.fasterxml.jackson.databind.misc;

import java.io.IOException;
import java.security.Permission;

import com.fasterxml.jackson.databind.*;

// Test(s) to verify that forced access works as expected
public class AccessFixTest extends BaseMapTest
{
    static class CauseBlockingSecurityManager
        extends SecurityManager
    {
        // Only block the thread that installed this manager: surefire runs test
        // classes in parallel, and a JVM-wide block breaks unrelated classes
        private final Thread owner = Thread.currentThread();

        @Override
        public void checkPermission(Permission perm) throws SecurityException {
            if (Thread.currentThread() == owner
                    && "suppressAccessChecks".equals(perm.getName())) {
                throw new SecurityException("Cannot force permission: "+perm);
            }
        }
    }

    // [databind#877]: avoid forcing access to `cause` field of `Throwable`
    // as it is never actually used (always call `initCause()` instead)
    public void testCauseOfThrowableIgnoral() throws Exception
    {
        final SecurityManager origSecMan = System.getSecurityManager();
        ObjectMapper mapper = jsonMapperBuilder()
                .disable(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS)
                .build();
        try {
            System.setSecurityManager(new CauseBlockingSecurityManager());
            _testCauseOfThrowableIgnoral(mapper);
        } finally {
            System.setSecurityManager(origSecMan);
        }
    }

    private void _testCauseOfThrowableIgnoral(ObjectMapper mapper) throws Exception
    {
        IOException e = mapper.readValue("{}", IOException.class);
        assertNotNull(e);
    }
}
