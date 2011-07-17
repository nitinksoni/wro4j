/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.Validate;

import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;

/**
 * Contains divers utility methods applied on processors.
 *
 * @author Alex Objelean
 * @created 21 Nov 2010
 */
public class ProcessorsUtils {
  /**
   * Returns a collection free of minimize aware processors (annotated with @Minimize).
   *
   * @param <T>
   *          type of processor
   * @param processors
   *          a collection of processors.
   */
  public static <T> Collection<T> getMinimizeFreeProcessors(final Collection<T> processors) {
    final Collection<T> result = new ArrayList<T>();
    for (final T processor : processors) {
      if (!processor.getClass().isAnnotationPresent(Minimize.class)) {
        result.add(processor);
      }
    }
    return result;
  }

  public static <T> boolean isMinimizeAwareProcessor(final T processor) {
    if (processor instanceof MinimizeAware) {
      return ((MinimizeAware)processor).isMinimize();
    }
    return processor.getClass().isAnnotationPresent(Minimize.class);
  }

  public static <T> SupportedResourceType getSupportedResourceType(final T processor) {
    SupportedResourceType supportedType = processor.getClass().getAnnotation(SupportedResourceType.class);
    /**
     * This is a special case for processors which implement {@link SupportedResourceTypeProvider} interface. This is
     * useful for decorator processors which needs to "inherit" the {@link SupportedResourceType} of the decorated
     * processor.
     */
    if (processor instanceof SupportedResourceTypeAware) {
      supportedType = ((SupportedResourceTypeAware) processor).getSupportedResourceType();
    }
    return supportedType;
  }

  /**
   * @param <T> processor type. Can be {@link ResourceProcessor}, {@link ResourcePostProcessor} or null (any).
   * @param type {@link ResourceType} to apply for searching on available processors.
   * @param availableProcessors a list where to perform the search.
   * @return a list of found processors which satisfy the search criteria. There are 3 possibilities:
   *        <ul>
   *          <li>If you search by null (any) type - you'll get only processors which can be applied on any resource (not any particular type)</li>
   *          <li>If you search by JS type - you'll get processors which can be applied on JS resources & any (null) resources </li>
   *          <li>If you search by CSS type - you'll get processors which can be applied on CSS resources & any (null) resources </li>
   *        </ul>
   */
  public static <T> Collection<T> getProcessorsByType(final ResourceType type, final Collection<T> availableProcessors) {
    Validate.notNull(availableProcessors);
    final Collection<T> found = new ArrayList<T>();
    for (final T processor : availableProcessors) {
      final SupportedResourceType supportedType = getSupportedResourceType(processor);
      final boolean isTypeSatisfied = supportedType == null || (supportedType != null && type == supportedType.value());
      if (isTypeSatisfied) {
        found.add(processor);
      }
    }
    return found;
  }

  /**
   * @return preProcessor of type processorClass if any found or null otherwise.
   */
  @SuppressWarnings("unchecked")
  public static final <T extends ResourceProcessor> T findPreProcessorByClass(final Class<T> processorClass,
    final Collection<ResourceProcessor> preProcessors) {
    T found = null;
    for (final ResourceProcessor processor : preProcessors) {
      if (processorClass.isInstance(processor)) {
        found = (T)processor;
        return found;
      }
    }
    return null;
  }
}