package com.mig35.injectorlib.utils.inject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;

/**
 * Date: 28.03.13
 * Time: 12:35
 *
 * @author MiG35
 */
public class Injector {

	private static final ArrayList<Class<? extends AnnotationProcessor>> sProcs;
	private ArrayList<AnnotationProcessor> mProcs;

	static {
		sProcs = new ArrayList<Class<? extends AnnotationProcessor>>();

		sProcs.add(ViewProcessor.class);
		sProcs.add(SavedStateProcessor.class);
	}

	public static Injector init(final Activity activity) {
		return init(activity.getClass());
	}

	public static Injector init(final Fragment fragment) {
		return init(fragment.getClass());
	}

	private static Injector init(final Class<?> clazz) {
		final Injector injector = new Injector();

		injector.collectMembers(clazz);

		return injector;
	}

	private void collectMembers(final Class<?> clazz) {
		mProcs = new ArrayList<AnnotationProcessor>();

		Class<?> klass = clazz;

		do {
			final Field[] fields = klass.getDeclaredFields();

			for (final Class<? extends AnnotationProcessor> annoProcClass : sProcs) {
				AnnotationProcessor annoProc = null;

				try {
					annoProc = annoProcClass.newInstance();
				}
				catch (final Exception e) {
					Log.d(getClass().getSimpleName(), "Could not create AnnotationProcessor", e);
				}

				if (annoProc != null) {
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

			if (Activity.class.equals(klass) || Fragment.class.equals(klass)) {
				klass = null;
			}
		} while (klass != null);
	}

	public void applyOnActivityCreate(final Activity activity, final Bundle savedState) {
		for (final AnnotationProcessor proc : mProcs) {
			if (proc instanceof ActivityAnnotationProcessor) {
				((ActivityAnnotationProcessor) proc).applyOnActivityCreate(activity, savedState);
			}
		}
	}

	public void applyOnActivityContentChange(final Activity activity) {
		for (final AnnotationProcessor proc : mProcs) {
			if (proc instanceof ActivityAnnotationProcessor) {
				((ActivityAnnotationProcessor) proc).applyOnActivityContentChange(activity);
			}
		}
	}

	public void applyOnActivitySaveInstanceState(final Activity activity, final Bundle outState) {
		for (final AnnotationProcessor proc : mProcs) {
			if (proc instanceof ActivityAnnotationProcessor) {
				((ActivityAnnotationProcessor) proc).applyOnActivitySaveInstanceState(activity, outState);
			}
		}
	}

	public void applyOnActivityDestroy(final Activity activity) {
		for (final AnnotationProcessor proc : mProcs) {
			if (proc instanceof ActivityAnnotationProcessor) {
				((ActivityAnnotationProcessor) proc).applyOnActivityDestroy(activity);
			}
		}
	}

	public void applyOnFragmentCreate(final Fragment fragment, final Bundle savedState) {
		for (final AnnotationProcessor proc : mProcs) {
			if (proc instanceof FragmentAnnotationProcessor) {
				((FragmentAnnotationProcessor) proc).applyOnFragmentCreate(fragment, savedState);
			}
		}
	}

	public void applyOnFragmentViewCreated(final Fragment fragment) {
		for (final AnnotationProcessor proc : mProcs) {
			if (proc instanceof FragmentAnnotationProcessor) {
				((FragmentAnnotationProcessor) proc).applyOnFragmentViewCreated(fragment);
			}
		}
	}

	public void applyOnFragmentSaveInstanceState(final Fragment fragment, final Bundle outState) {
		for (final AnnotationProcessor proc : mProcs) {
			if (proc instanceof FragmentAnnotationProcessor) {
				((FragmentAnnotationProcessor) proc).applyOnFragmentSaveInstanceState(fragment, outState);
			}
		}
	}

	public void applyOnFragmentDestroyView(final Fragment fragment) {
		for (final AnnotationProcessor proc : mProcs) {
			if (proc instanceof FragmentAnnotationProcessor) {
				((FragmentAnnotationProcessor) proc).applyOnFragmentDestroyView(fragment);
			}
		}
	}

	protected abstract static class AnnotationProcessor {

		private ArrayList<Field> mFields;

		protected abstract boolean checkField(Field field);

		protected ArrayList<Field> getFields() {
			if (mFields == null) {
				mFields = new ArrayList<Field>();
			}

			return mFields;
		}
	}

	protected static class ViewProcessor extends AnnotationProcessor implements FragmentAnnotationProcessor, ActivityAnnotationProcessor {

		@Override
		public boolean checkField(final Field field) {
			if (field.isAnnotationPresent(InjectView.class)) {
				if (Modifier.isStatic(field.getModifiers())) {
					throw new IllegalStateException("InjectView-field may not be static.");
				}
				else if (Modifier.isFinal(field.getModifiers())) {
					throw new IllegalStateException("InjectView-field may not be final.");
				}
				else if (!View.class.isAssignableFrom(field.getType())) {
					throw new IllegalStateException("InjectView-field must extends View.");
				}

				getFields().add(field);

				return true;
			}

			return false;
		}

		@Override
		public void applyOnFragmentCreate(final Fragment fragment, final Bundle savedState) {
		}

		@Override
		public void applyOnFragmentViewCreated(final Fragment fragment) {
			for (final Field field : getFields()) {
				final InjectView injectView = field.getAnnotation(InjectView.class);

				final int id = injectView.value();

				final View view = fragment.getView().findViewById(id);

				if (view == null) {
					if (injectView.optional()) {
						continue;
					}

					throw new IllegalStateException("Could not get View for Fragment: " + fragment.getClass().getSimpleName() + ", with id: " + id);
				}

				try {
					field.set(fragment, view);
				}
				catch (final IllegalArgumentException e) {
					throw new IllegalStateException(
							"Could not assign View in Fragment: " + fragment.getClass().getSimpleName() + ", to Field: " + field.getName(), e);
				}
				catch (final IllegalAccessException e) {
					throw new IllegalStateException(
							"Could not assign View in Fragment: " + fragment.getClass().getSimpleName() + ", to Field: " + field.getName(), e);
				}
			}
		}

		@Override
		public void applyOnActivityCreate(final Activity activity, final Bundle savedState) {
		}

		@Override
		public void applyOnActivityContentChange(final Activity activity) {
			for (final Field field : getFields()) {
				final InjectView injectView = field.getAnnotation(InjectView.class);

				if (injectView != null) {
					final int id = injectView.value();

					final View view = activity.findViewById(id);

					if (view == null) {
						if (injectView.optional()) {
							continue;
						}

						throw new IllegalStateException(
								"Could not get view from Activity: " + activity.getClass().getSimpleName() + ", with id: " + id);
					}

					try {
						field.set(activity, view);
					}
					catch (final IllegalArgumentException e) {
						throw new IllegalStateException(
								"Could not assign view in Activity: " + activity.getClass().getSimpleName() + ", to Field: " + field.getName(), e);
					}
					catch (final IllegalAccessException e) {
						throw new IllegalStateException(
								"Could not assign view in Activity: " + activity.getClass().getSimpleName() + ", to Field: " + field.getName(), e);
					}
				}
			}
		}

		@Override
		public void applyOnFragmentSaveInstanceState(final Fragment fragment, final Bundle outState) {
		}

		@Override
		public void applyOnActivitySaveInstanceState(final Activity activity, final Bundle outState) {
		}

		@Override
		public void applyOnActivityDestroy(final Activity activity) {
			for (final Field field : getFields()) {
				try {
					if (field.isAnnotationPresent(InjectView.class)) {
						field.set(activity, null);
					}
				}
				catch (final IllegalArgumentException e) {
					throw new IllegalStateException(
							"Could not assign null (onDestroyView) in Activity: " + activity.getClass().getSimpleName() + ", to Field: " +
									field.getName(), e);
				}
				catch (final IllegalAccessException e) {
					throw new IllegalStateException(
							"Could not assign null  (onDestroyView) Activity: " + activity.getClass().getSimpleName() + ", to Field: " +
									field.getName(), e);
				}
			}
		}

		@Override
		public void applyOnFragmentDestroyView(final Fragment fragment) {
			for (final Field field : getFields()) {
				try {
					if (field.isAnnotationPresent(InjectView.class)) {
						field.set(fragment, null);
					}
				}
				catch (final IllegalArgumentException e) {
					throw new IllegalStateException(
							"Could not assign null (onDestroyView) in Fragment: " + fragment.getClass().getSimpleName() + ", to Field: " +
									field.getName(), e);
				}
				catch (final IllegalAccessException e) {
					throw new IllegalStateException(
							"Could not assign null  (onDestroyView) Fragment: " + fragment.getClass().getSimpleName() + ", to Field: " +
									field.getName(), e);
				}
			}
		}
	}

	protected static class SavedStateProcessor extends AnnotationProcessor implements ActivityAnnotationProcessor, FragmentAnnotationProcessor {

		@Override
		protected boolean checkField(final Field field) {
			if (field.isAnnotationPresent(InjectSavedState.class)) {
				if (Modifier.isStatic(field.getModifiers())) {
					throw new IllegalStateException("InjectSavedState-field may not be static.");
				}
				else if (Modifier.isFinal(field.getModifiers())) {
					throw new IllegalStateException("InjectSavedState-field may not be final.");
				}

				getFields().add(field);

				return true;
			}

			return false;
		}

		@Override
		public void applyOnFragmentCreate(final Fragment fragment, final Bundle savedState) {
			for (final Field field : getFields()) {
				final InjectSavedState injectSavedState = field.getAnnotation(InjectSavedState.class);

				String tag = injectSavedState.value();

				if (TextUtils.isEmpty(tag)) {
					tag = field.getDeclaringClass().getName() + '#' + field.getName();

					final String fragTag = fragment.getTag();

					if (fragTag != null) {
						tag += ':' + fragTag;
					}

					tag += ":" + fragment.getId();
				}

				if (savedState == null || !savedState.containsKey(tag)) {
					continue;
				}

				final Object object = savedState.get(tag);

				try {
					field.set(fragment, object);
				}
				catch (final IllegalArgumentException e) {
					throw new IllegalStateException(
							"Could not assing SavedState in Fragment: " + fragment.getClass().getSimpleName() + " to Field: " + field.getName(), e);
				}
				catch (final IllegalAccessException e) {
					throw new IllegalStateException(
							"Could not assing SavedState in Fragment: " + fragment.getClass().getSimpleName() + " to Field: " + field.getName(), e);
				}
			}
		}

		@Override
		public void applyOnFragmentViewCreated(final Fragment fragment) {
		}

		@Override
		public void applyOnActivityCreate(final Activity activity, final Bundle savedState) {
			for (final Field field : getFields()) {
				final InjectSavedState injectSavedState = field.getAnnotation(InjectSavedState.class);

				String tag = injectSavedState.value();

				if (TextUtils.isEmpty(tag)) {
					tag = field.getDeclaringClass().getName() + "#" + field.getName();
				}

				if (savedState == null || !savedState.containsKey(tag)) {
					continue;
				}

				final Object object = savedState.get(tag);

				try {
					field.set(activity, object);
				}
				catch (final IllegalArgumentException e) {
					throw new IllegalStateException(
							"Could not assing SavedState in Activity: " + activity.getClass().getSimpleName() + " to Field: " + field.getName(), e);
				}
				catch (final IllegalAccessException e) {
					throw new IllegalStateException(
							"Could not assing SavedState in Activity: " + activity.getClass().getSimpleName() + " to Field: " + field.getName(), e);
				}
			}
		}

		@Override
		public void applyOnActivityContentChange(final Activity activity) {
		}

		@SuppressWarnings("unchecked")
		private void processOutState(final Object object, final Bundle outState) {
			for (final Field field : getFields()) {
				final InjectSavedState injectSavedState = field.getAnnotation(InjectSavedState.class);

				String tag = injectSavedState.value();

				if (TextUtils.isEmpty(tag)) {
					tag = field.getDeclaringClass().getName() + '#' + field.getName();

					if (object instanceof Fragment) {
						final String fragTag = ((Fragment) object).getTag();

						if (fragTag != null) {
							tag += ':' + fragTag;
						}

						tag += ":" + ((Fragment) object).getId();
					}
				}

				boolean success = false;

				try {
					final Object value = field.get(object);

					if (value == null) {
						continue;
					}

					if (value instanceof Boolean) {
						outState.putBoolean(tag, (Boolean) value);
						success = true;
					}
					else if (value instanceof boolean[]) {
						outState.putBooleanArray(tag, (boolean[]) value);
						success = true;
					}
					else if (value instanceof Byte) {
						outState.putByte(tag, (Byte) value);
						success = true;
					}
					else if (value instanceof byte[]) {
						outState.putByteArray(tag, (byte[]) value);
						success = true;
					}
					else if (value instanceof Character) {
						outState.putChar(tag, (Character) value);
						success = true;
					}
					else if (value instanceof char[]) {
						outState.putCharArray(tag, (char[]) value);
						success = true;
					}
					else if (value instanceof Double) {
						outState.putDouble(tag, (Double) value);
						success = true;
					}
					else if (value instanceof double[]) {
						outState.putDoubleArray(tag, (double[]) value);
						success = true;
					}
					else if (value instanceof Float) {
						outState.putFloat(tag, (Float) value);
						success = true;
					}
					else if (value instanceof float[]) {
						outState.putFloatArray(tag, (float[]) value);
						success = true;
					}
					else if (value instanceof Integer) {
						outState.putInt(tag, (Integer) value);
						success = true;
					}
					else if (value instanceof int[]) {
						outState.putIntArray(tag, (int[]) value);
						success = true;
					}
					else if (value instanceof Long) {
						outState.putLong(tag, (Long) value);
						success = true;
					}
					else if (value instanceof long[]) {
						outState.putLongArray(tag, (long[]) value);
						success = true;
					}
					else if (value instanceof Short) {
						outState.putShort(tag, (Short) value);
						success = true;
					}
					else if (value instanceof short[]) {
						outState.putShortArray(tag, (short[]) value);
						success = true;
					}
					else if (value instanceof String) {
						outState.putString(tag, (String) value);
						success = true;
					}
					else if (value instanceof String[]) {
						outState.putStringArray(tag, (String[]) value);
						success = true;
					}
					else if (value instanceof CharSequence) {
						outState.putCharSequence(tag, (CharSequence) value);
						success = true;
					}
					else if (value instanceof CharSequence[]) {
						outState.putCharSequenceArray(tag, (CharSequence[]) value);
						success = true;
					}
					else if (value instanceof Bundle) {
						outState.putBundle(tag, (Bundle) value);
						success = true;
					}
					else if (value instanceof Parcelable) {
						outState.putParcelable(tag, (Parcelable) value);
						success = true;
					}
					else if (value instanceof Parcelable[]) {
						outState.putParcelableArray(tag, (Parcelable[]) value);
						success = true;
					}
					else if (value instanceof ArrayList) {
						final Type type = field.getGenericType();

						if (type instanceof ParameterizedType) {
							final ParameterizedType paramType = (ParameterizedType) type;

							final Type[] typeArguments = paramType.getActualTypeArguments();

							if (typeArguments.length == 1) {
								final Type oneType = typeArguments[0];

								if (oneType instanceof Class) {
									final Class<?> oneClass = (Class<?>) oneType;

									if (String.class.isAssignableFrom(oneClass)) {
										outState.putStringArrayList(tag, (ArrayList<String>) value);
										success = true;
									}
									else if (CharSequence.class.isAssignableFrom(oneClass)) {
										outState.putCharSequenceArrayList(tag, (ArrayList<CharSequence>) value);
										success = true;
									}
									else if (Integer.class.isAssignableFrom(oneClass)) {
										outState.putIntegerArrayList(tag, (ArrayList<Integer>) value);
										success = true;
									}
									else if (Parcelable.class.isAssignableFrom(oneClass)) {
										outState.putIntegerArrayList(tag, (ArrayList<Integer>) value);
										success = true;
									}
								}
								else if (oneType instanceof WildcardType) {
									final WildcardType wildType = (WildcardType) oneType;

									final Type[] upperBounds = wildType.getUpperBounds();

									if (wildType.getLowerBounds().length == 0 && upperBounds.length == 1) {
										final Type oneInnerType = upperBounds[0];

										if (oneInnerType instanceof Class) {
											final Class<?> oneClass = (Class<?>) oneInnerType;

											if (Parcelable.class.isAssignableFrom(oneClass)) {
												outState.putParcelableArrayList(tag, (ArrayList<? extends Parcelable>) value);
												success = true;
											}
										}
									}
								}
							}
						}
					}
					else if (value instanceof SparseArray) {
						final Type type = field.getGenericType();

						if (type instanceof ParameterizedType) {
							final ParameterizedType paramType = (ParameterizedType) type;

							final Type[] typeArguments = paramType.getActualTypeArguments();

							if (typeArguments.length == 1) {
								final Type oneType = typeArguments[0];

								if (oneType instanceof Class) {
									final Class<?> oneClass = (Class<?>) oneType;

									if (Parcelable.class.isAssignableFrom(oneClass)) {
										outState.putSparseParcelableArray(tag, (SparseArray<? extends Parcelable>) value);
										success = true;
									}
								}
								else if (oneType instanceof WildcardType) {
									final WildcardType wildType = (WildcardType) oneType;

									final Type[] upperBounds = wildType.getUpperBounds();

									if (wildType.getLowerBounds().length == 0 && upperBounds.length == 1) {
										final Type oneInnerType = upperBounds[0];

										if (oneInnerType instanceof Class) {
											final Class<?> oneClass = (Class<?>) oneInnerType;

											if (Parcelable.class.isAssignableFrom(oneClass)) {
												outState.putSparseParcelableArray(tag, (SparseArray<? extends Parcelable>) value);
												success = true;
											}
										}
									}
								}
							}
						}
					}

					if (!success && value instanceof Serializable) {
						outState.putSerializable(tag, (Serializable) value);
						success = true;
					}

					if (!success) {
						throw new IllegalStateException(
								"Could not save value: " + value + " for field " + field.getName() + " in " + object.getClass().getName());
					}
				}
				catch (final IllegalArgumentException e) {
					throw new IllegalStateException("Could not save state for field " + field.getName() + " in " + object.getClass().getName(), e);
				}
				catch (final IllegalAccessException e) {
					throw new IllegalStateException("Could not save state for field " + field.getName() + " in " + object.getClass().getName(), e);
				}
				catch (final ClassCastException e) {
					throw new IllegalStateException("Could not save state for field " + field.getName() + " in " + object.getClass().getName(), e);
				}
			}
		}

		@Override
		public void applyOnFragmentSaveInstanceState(final Fragment fragment, final Bundle outState) {
			processOutState(fragment, outState);
		}

		@Override
		public void applyOnActivitySaveInstanceState(final Activity activity, final Bundle outState) {
			processOutState(activity, outState);
		}

		@Override
		public void applyOnActivityDestroy(final Activity activity) {
		}

		@Override
		public void applyOnFragmentDestroyView(final Fragment fragment) {
		}
	}

	protected interface ActivityAnnotationProcessor {

		void applyOnActivityCreate(final Activity activity, final Bundle savedState);

		void applyOnActivityContentChange(final Activity activity);

		void applyOnActivitySaveInstanceState(final Activity activity, final Bundle outState);

		void applyOnActivityDestroy(final Activity activity);
	}

	protected interface FragmentAnnotationProcessor {

		void applyOnFragmentCreate(final Fragment fragment, final Bundle savedState);

		void applyOnFragmentViewCreated(final Fragment fragment);

		void applyOnFragmentSaveInstanceState(final Fragment fragment, final Bundle outState);

		void applyOnFragmentDestroyView(final Fragment fragment);
	}
}