package de.dosmike.spongepowered.oreapi.utility;

import de.dosmike.spongepowered.oreapi.ConnectionManager;

public class TypeMappers {

    // this mapper should not be used
    public static class IdentityMapper implements TypeMapper<Object,Object> {
        @Override
        public Class<Object> getInputType() {
            return Object.class;
        }
        @Override
        public Class<Object> getOutputType() {
            return Object.class;
        }
        @Override
        public Object apply(Object x) {
            return x;
        }
    }
    public static class StringTimestampMapper implements TypeMapper<String, Long> {
        @Override
        public Class<String> getInputType() {
            return String.class;
        }
        @Override
        public Class<Long> getOutputType() {
            return Long.class;
        }
        @Override
        public Long apply(String s) {
            return RepositoryTimestamp.toNative(s);
        }
    }

}
