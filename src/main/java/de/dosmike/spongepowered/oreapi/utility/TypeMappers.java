package de.dosmike.spongepowered.oreapi.utility;

public class TypeMappers {

    // this mapper should not be used
    public static class IdentityMapper implements TypeMapper<Object, Object> {
        @Override
        public Class<Object> getSourceType() {
            return Object.class;
        }

        @Override
        public Class<Object> getNativeType() {
            return Object.class;
        }

        @Override
        public Object fromSource(Object x) {
            return x;
        }

        @Override
        public Object fromNative(Object x) {
            return x;
        }
    }

    public static class StringTimestampMapper implements TypeMapper<String, Long> {
        @Override
        public Class<String> getSourceType() {
            return String.class;
        }

        @Override
        public Class<Long> getNativeType() {
            return Long.class;
        }

        @Override
        public Long fromSource(String s) {
            return RepositoryTimestamp.toNative(s);
        }

        @Override
        public String fromNative(Long x) {
            return RepositoryTimestamp.fromNative(x);
        }
    }

}
