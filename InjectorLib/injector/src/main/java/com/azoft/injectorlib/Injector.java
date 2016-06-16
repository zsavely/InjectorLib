package com.azoft.injectorlib;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class Injector {

    private static final List<Class<? extends AnnotationProcessor>> PROCS;
    private List<AnnotationProcessor> mProcs;

    static {
        PROCS = new ArrayList<>();

        PROCS.add(SavedStateProcessor.class);
    }

    private Injector() {
    }

    public static Injector init(@NonNull final Object model) {
        return init(model.getClass());
    }

    public static Injector init(@NonNull final Class<?> clazz) {
        final Injector injector = new Injector();

        injector.collectMembers(clazz);

        return injector;
    }

    private void collectMembers(final Class<?> clazz) {
        mProcs = new ArrayList<>();

        Class<?> klass = clazz;

        do {
            final Field[] fields = klass.getDeclaredFields();

            for (final Class<? extends AnnotationProcessor> annoProcClass : PROCS) {
                AnnotationProcessor annoProc = null;

                try {
                    annoProc = annoProcClass.getConstructor().newInstance();
                } catch (final Exception e) {
                    Log.d(getClass().getSimpleName(), "Could not create AnnotationProcessor", e);
                }

                if (null != annoProc) {
                    boolean checked = false;

                    for (final Field field : fields) {
                        field.setAccessible(true);

                        checked = annoProc.checkField(field) || checked;
                    }

                    if (checked) {
                        mProcs.add(annoProc);
                    }
                }
            }

            klass = klass.getSuperclass();

            if (Activity.class.equals(klass) || Object.class.equals(klass)) {
                klass = null;
            }
        } while (null != klass);
    }

    public void applyRestoreInstanceState(@NonNull final Object model, @Nullable final Bundle savedState) {
        for (final AnnotationProcessor proc : mProcs) {
            if (proc instanceof BaseAnnotationProcessor) {
                ((BaseAnnotationProcessor) proc).applyOnCreate(model, savedState);
            }
        }
    }

    public void applyOnSaveInstanceState(@NonNull final Object model, @NonNull final Bundle outState) {
        for (final AnnotationProcessor proc : mProcs) {
            if (proc instanceof BaseAnnotationProcessor) {
                ((BaseAnnotationProcessor) proc).applyOnSaveInstanceState(model, outState);
            }
        }
    }

    abstract static class AnnotationProcessor {

        private List<Field> mFields;

        protected abstract boolean checkField(Field field);

        protected List<Field> getFields() {
            if (null == mFields) {
                mFields = new ArrayList<>();
            }

            return mFields;
        }
    }

    static class SavedStateProcessor extends AnnotationProcessor implements BaseAnnotationProcessor {

        @Override
        protected boolean checkField(final Field field) {
            if (field.isAnnotationPresent(InjectSavedState.class)) {
                if (Modifier.isStatic(field.getModifiers())) {
                    throw new IllegalStateException("InjectSavedState-field may not be static.");
                } else if (Modifier.isFinal(field.getModifiers())) {
                    throw new IllegalStateException("InjectSavedState-field may not be final.");
                }

                getFields().add(field);

                return true;
            }

            return false;
        }

        @Override
        public void applyOnCreate(@NonNull final Object model, @Nullable final Bundle savedState) {
            for (final Field field : getFields()) {
                final InjectSavedState injectSavedState = field.getAnnotation(InjectSavedState.class);

                final String tagString = generateTagString(model, field, injectSavedState);

                if (null == savedState || !savedState.containsKey(tagString)) {
                    continue;
                }

                final Object object = savedState.get(tagString);

                try {
                    field.set(model, object);
                } catch (final IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalStateException(
                            "Could not assing SavedState in " + model.getClass().getSimpleName() + " to Field: " + field.getName(), e);
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void applyOnSaveInstanceState(@NonNull final Object model, @NonNull final Bundle outState) {
            for (final Field field : getFields()) {
                final InjectSavedState injectSavedState = field.getAnnotation(InjectSavedState.class);

                final String tagString = generateTagString(model, field, injectSavedState);

                boolean success = false;

                try {
                    final Object value = field.get(model);

                    if (null == value) {
                        continue;
                    }

                    if (value instanceof Boolean) {
                        outState.putBoolean(tagString, (Boolean) value);
                        success = true;
                    } else if (value instanceof boolean[]) {
                        outState.putBooleanArray(tagString, (boolean[]) value);
                        success = true;
                    } else if (value instanceof Byte) {
                        outState.putByte(tagString, (Byte) value);
                        success = true;
                    } else if (value instanceof byte[]) {
                        outState.putByteArray(tagString, (byte[]) value);
                        success = true;
                    } else if (value instanceof Character) {
                        outState.putChar(tagString, (Character) value);
                        success = true;
                    } else if (value instanceof char[]) {
                        outState.putCharArray(tagString, (char[]) value);
                        success = true;
                    } else if (value instanceof Double) {
                        outState.putDouble(tagString, (Double) value);
                        success = true;
                    } else if (value instanceof double[]) {
                        outState.putDoubleArray(tagString, (double[]) value);
                        success = true;
                    } else if (value instanceof Float) {
                        outState.putFloat(tagString, (Float) value);
                        success = true;
                    } else if (value instanceof float[]) {
                        outState.putFloatArray(tagString, (float[]) value);
                        success = true;
                    } else if (value instanceof Integer) {
                        outState.putInt(tagString, (Integer) value);
                        success = true;
                    } else if (value instanceof int[]) {
                        outState.putIntArray(tagString, (int[]) value);
                        success = true;
                    } else if (value instanceof Long) {
                        outState.putLong(tagString, (Long) value);
                        success = true;
                    } else if (value instanceof long[]) {
                        outState.putLongArray(tagString, (long[]) value);
                        success = true;
                    } else if (value instanceof Short) {
                        outState.putShort(tagString, (Short) value);
                        success = true;
                    } else if (value instanceof short[]) {
                        outState.putShortArray(tagString, (short[]) value);
                        success = true;
                    } else if (value instanceof String) {
                        outState.putString(tagString, (String) value);
                        success = true;
                    } else if (value instanceof String[]) {
                        outState.putStringArray(tagString, (String[]) value);
                        success = true;
                    } else if (value instanceof CharSequence) {
                        outState.putCharSequence(tagString, (CharSequence) value);
                        success = true;
                    } else if (value instanceof CharSequence[]) {
                        outState.putCharSequenceArray(tagString, (CharSequence[]) value);
                        success = true;
                    } else if (value instanceof Bundle) {
                        outState.putBundle(tagString, (Bundle) value);
                        success = true;
                    } else if (value instanceof Parcelable) {
                        outState.putParcelable(tagString, (Parcelable) value);
                        success = true;
                    } else if (value instanceof Parcelable[]) {
                        outState.putParcelableArray(tagString, (Parcelable[]) value);
                        success = true;
                    } else if (value instanceof ArrayList) {
                        final Type type = field.getGenericType();

                        if (type instanceof ParameterizedType) {
                            final ParameterizedType paramType = (ParameterizedType) type;

                            final Type[] typeArguments = paramType.getActualTypeArguments();

                            if (1 == typeArguments.length) {
                                final Type oneType = typeArguments[0];

                                if (oneType instanceof Class) {
                                    final Class<?> oneClass = (Class<?>) oneType;

                                    if (String.class.isAssignableFrom(oneClass)) {
                                        outState.putStringArrayList(tagString, (ArrayList<String>) value);
                                        success = true;
                                    } else if (CharSequence.class.isAssignableFrom(oneClass)) {
                                        outState.putCharSequenceArrayList(tagString, (ArrayList<CharSequence>) value);
                                        success = true;
                                    } else if (Integer.class.isAssignableFrom(oneClass)) {
                                        outState.putIntegerArrayList(tagString, (ArrayList<Integer>) value);
                                        success = true;
                                    } else if (Parcelable.class.isAssignableFrom(oneClass)) {
                                        outState.putIntegerArrayList(tagString, (ArrayList<Integer>) value);
                                        success = true;
                                    }
                                } else if (oneType instanceof WildcardType) {
                                    final WildcardType wildType = (WildcardType) oneType;

                                    final Type[] upperBounds = wildType.getUpperBounds();

                                    if (0 == wildType.getLowerBounds().length && 1 == upperBounds.length) {
                                        final Type oneInnerType = upperBounds[0];

                                        if (oneInnerType instanceof Class) {
                                            final Class<?> oneClass = (Class<?>) oneInnerType;

                                            if (Parcelable.class.isAssignableFrom(oneClass)) {
                                                outState.putParcelableArrayList(tagString, (ArrayList<? extends Parcelable>) value);
                                                success = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (value instanceof SparseArray) {
                        final Type type = field.getGenericType();

                        if (type instanceof ParameterizedType) {
                            final ParameterizedType paramType = (ParameterizedType) type;

                            final Type[] typeArguments = paramType.getActualTypeArguments();

                            if (1 == typeArguments.length) {
                                final Type oneType = typeArguments[0];

                                if (oneType instanceof Class) {
                                    final Class<?> oneClass = (Class<?>) oneType;

                                    if (Parcelable.class.isAssignableFrom(oneClass)) {
                                        outState.putSparseParcelableArray(tagString, (SparseArray<? extends Parcelable>) value);
                                        success = true;
                                    }
                                } else if (oneType instanceof WildcardType) {
                                    final WildcardType wildType = (WildcardType) oneType;

                                    final Type[] upperBounds = wildType.getUpperBounds();

                                    if (0 == wildType.getLowerBounds().length && 1 == upperBounds.length) {
                                        final Type oneInnerType = upperBounds[0];

                                        if (oneInnerType instanceof Class) {
                                            final Class<?> oneClass = (Class<?>) oneInnerType;

                                            if (Parcelable.class.isAssignableFrom(oneClass)) {
                                                outState.putSparseParcelableArray(tagString, (SparseArray<? extends Parcelable>) value);
                                                success = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!success && value instanceof Serializable) {
                        outState.putSerializable(tagString, (Serializable) value);
                        success = true;
                    }

                    if (!success) {
                        throw new IllegalStateException(
                                "Could not save value: " + value + " for field " + field.getName() + " in " + model.getClass().getName());
                    }
                } catch (final IllegalArgumentException | IllegalAccessException | ClassCastException e) {
                    throw new IllegalStateException("Could not save state for field " + field.getName() + " in " + model.getClass().getName(), e);
                }
            }
        }

        private String generateTagString(final Object model, final Field field, final InjectSavedState injectSavedState) {
            final StringBuilder tag = new StringBuilder(injectSavedState.value());

            if (0 == tag.length()) {
                tag.append(field.getDeclaringClass().getName());
                tag.append('#');
                tag.append(field.getName());

                if (model instanceof InjectSaveStateTag) {
                    tag.append(':');
                    tag.append(((InjectSaveStateTag) model).getSaveStateTag());
                } else {
                    appendTagIfFragment(tag, model);
                }
            }

            return tag.toString();
        }

        private void appendTagIfFragment(final StringBuilder tag, final Object model) {
            //noinspection TryWithIdenticalCatches
            try {
                Class<?> fragmentClass = getFragmentClassIfHave(model, "android.app.Fragment");
                if (null == fragmentClass) {
                    fragmentClass = getFragmentClassIfHave(model, "android.support.v4.app.Fragment");
                }
                if (null != fragmentClass) {
                    final Method tagMethod = fragmentClass.getDeclaredMethod("getTag");
                    final Object fragmentTag = tagMethod.invoke(model);

                    if (null != fragmentTag) {
                        tag.append(':');
                        tag.append(fragmentTag);
                    }

                    final Method idMethod = fragmentClass.getDeclaredMethod("getId");
                    tag.append(':');
                    tag.append(idMethod.invoke(model));
                }
            } catch (final NoSuchMethodException ignored) {
                // pass
            } catch (final InvocationTargetException ignored) {
                // pass
            } catch (final IllegalAccessException ignored) {
                // pass
            }
        }

        private Class<?> getFragmentClassIfHave(final Object model, final String className) {
            try {
                final Class<?> fragmentClass = Class.forName(className);
                if (fragmentClass.isAssignableFrom(model.getClass())) {
                    return fragmentClass;
                }
            } catch (final ClassNotFoundException ignored) {
                // pass
            }
            return null;
        }
    }

    interface BaseAnnotationProcessor {

        void applyOnCreate(@NonNull final Object model, @Nullable final Bundle savedState);

        void applyOnSaveInstanceState(@NonNull final Object model, @NonNull final Bundle outState);
    }
}