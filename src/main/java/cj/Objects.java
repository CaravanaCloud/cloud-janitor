package cj;

import cj.shell.ShellTask;
import cj.spi.Task;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

@ApplicationScoped
public class Objects {
    @Inject
    Logger log;

    @Inject
    InputsMap inputs;

    @Inject
    Tasks tasks;

    @Inject
    BeanManager bm;

    @Inject
    CJConfiguration config;

    @Inject
    InputsMap inputsMap;

    @Inject
    Instance<ShellTask> shellInstance;

    public List<TaskConfiguration> allTaskConfigurations() {
        //TODO: Unified Java + Config task configurations
        var taskBeans = bm.getBeans(Task.class);
        var taskConfigsJava = taskBeans
                .stream()
                .map(this::getJavaTaskConfig);
        var fromJava = taskConfigsJava
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        var fromConfig = config.tasks().orElse(List.of());
        var result = Stream.concat(fromJava.stream(),
                        fromConfig.stream())
                .toList();
        var sorted = result.stream()
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(TaskConfiguration::name))
                .toList();
        return sorted;
    }

    private Optional<TaskConfiguration> getJavaTaskConfig(Bean<?> bean) {
        var name = bean.getName();
        var description = getAnnotationString(bean, TaskDescription.class);
        var maturity = getAnnotationString(bean, TaskMaturity.class);
        var repeat = (TaskRepeat) getAnnotationValue(bean, TaskRepeater.class);
        //TODO: Build annotations for input config and bypass
        var inputs = inputsMap.getInputsForTask(name);
        var bypass = (List<String>) null;
        var taskConfig = TaskConfigurationRecord.of(name,
                description,
                maturity,
                inputs,
                bypass,
                repeat);
        return Optional.ofNullable(taskConfig);
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

    public <A extends Annotation> A getAnnotation(Object object, Class<A> annotation) {
        checkNotNull(object);
        return getAnnotation(object.getClass(), annotation);
    }


    public <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotation) {
        var annot = clazz.getAnnotation(annotation);
        if (annot == null){
            var superclazz = clazz.getSuperclass();
            annot = superclazz.getAnnotation(annotation);
        }
        return annot;
    }

    public List<Input> getExpectedInputs(Class<? extends Task> clazz) {
        var expectedInputs = (String[]) getAnnotationValue(clazz, ExpectedInputs.class);
        if (expectedInputs == null) return List.of();
        @SuppressWarnings("redundant")
        var taskInputs = Arrays.stream(expectedInputs)
                .map(inputs::findInputByName)
                .toList();
        return taskInputs;
    }


    public List<? extends Task> createTasksByName(String taskName) {
        return bm.getBeans(taskName)
                .stream()
                .map(this::fromBean)
                .toList();
    }
    
    private Task fromBean(Bean<?> bean) {
        var ctx = bm.createCreationalContext(bean);
        try {
            var ref = bm.getReference(bean, bean.getBeanClass(), ctx);
            if (ref instanceof Task aTask) {
                return aTask;
            } else {
                log.error("Bean {} is not a Task", bean);
                throw new IllegalArgumentException("Bean is not a task");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Failed to create task from bean {}", bean, ex);
            throw ex;
        }
    }
}
