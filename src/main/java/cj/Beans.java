package cj;

import cj.spi.Task;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class Beans {
    @Inject
    Logger log;

    @Inject
    InputsMap inputs;

    public TaskConfiguration configFromBean(Bean<?> bean) {
        var name = bean.getName();
        var taskDescription = getAnnotationString(bean, TaskDescription.class);
        var taskMaturity = getAnnotationString(bean, TaskMaturity.class);
        if (name == null || taskDescription == null) {
            log.trace("Ignoring bean {}.", bean);
            return null;
        }
        var taskInputNames = getAnnotationStrings(bean, ExpectedInputs.class);
        List<InputConfig> taskInputs = new ArrayList<>();
        if (taskInputNames != null) {
            taskInputs = Arrays.stream(taskInputNames)
                    .map(inputs::findInputConfigByName)
                    .toList();
        }
        return new SimpleTaskConfiguration(name,
                taskDescription,
                taskMaturity,
                taskInputs);
    }

    public <A extends Annotation> String[] getAnnotationStrings(Bean<?> bean, Class<A> annotation) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        var value = (String[]) getAnnotationValue(bean, annotation);
        return value;
    }

    public <A extends Annotation> String getAnnotationString(Bean<?> bean, Class<A> annotation) {
        var value = getAnnotationValue(bean, annotation);
        return value == null ? null : value.toString();
    }

    public <A extends Annotation> Object getAnnotationValue(Bean<?> bean, Class<A> annotation) {
        var clazz = bean.getBeanClass();
        return getAnnotationValue(clazz, annotation);
    }

    public <A extends Annotation> Object getAnnotationValue(Class<?> clazz, Class<A> annotation) {
        A annot = getAnnotation(clazz, annotation);
        if (annot == null) return null;
        var valueMethod = Arrays.stream(annotation.getMethods()).filter(m -> m.getName().equals("value")).findFirst();
        if (valueMethod.isPresent()){
            var value = (Object) null;
            try {
                value = valueMethod.get().invoke(annot);
            } catch (IllegalAccessException e) {
                log.error("IllegalAccessException", e);
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                log.error("Failed to get annotation value", e);
                throw new RuntimeException(e);
            }
            return value;
        }
        return null;
    }

    private static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotation) {
        var annot = clazz.getAnnotation(annotation);
        if (annot == null){
            var superclazz = clazz.getSuperclass();
            annot = superclazz.getAnnotation(annotation);
        }
        return annot;
    }

    public List<Input> getExpectedInputs(Task task) {
        var expectedInputs = (String[]) getAnnotationValue(task.getClass(), ExpectedInputs.class);
        if (expectedInputs == null) return List.of();
        @SuppressWarnings("redundant")
        var taskInputs = Arrays.stream(expectedInputs)
                .map(inputs::findInputByName)
                .toList();
        return taskInputs;
    }
}
