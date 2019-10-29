package lib.tartard.mybatis.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.List;
import java.util.Optional;

/**
 * Add Jackson annotation to getters, and eventually to the constructor and the setters.
 * 
 * @author Alexandre Hausherr
 * @version 1
 */
public class JsonAnnotationsPlugin extends PluginAdapter {

    private static final String FIELD_PREFIX = "FIELD_";
    
    
    private static String getFieldNameConstantName(String fieldName) {
        return FIELD_PREFIX + fieldName.toUpperCase();
    }
    
    @Override
    public boolean validate(List<String> list) {
        return true;
    }


    /**
     * For each non-static field of the class, add a public static String field holding this field's name. 
     * The field name will be used in @JsonGetter, @JsonSetter and @JsonProperty annotations.
     * @param field
     * @param topLevelClass
     * @param introspectedColumn
     * @param introspectedTable
     * @param modelClassType
     * @return
     */
    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        Field staticFieldName = new Field();
        staticFieldName.setFinal(true);
        staticFieldName.setVisibility(JavaVisibility.PUBLIC);
        staticFieldName.setStatic(true);
        staticFieldName.setName(getFieldNameConstantName(field.getName()) + " = \"" + field.getName() + "\"");
        staticFieldName.setType(new FullyQualifiedJavaType("java.lang.String"));
        topLevelClass.addField(staticFieldName);
        
        return true;
    }


    /**
     * Add a @Jsongetter annotation for each getter of the class being generated.
     * @param method
     * @param topLevelClass
     * @param introspectedColumn
     * @param introspectedTable
     * @param modelClassType
     * @return
     */
    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, Plugin.ModelClassType modelClassType) {
        topLevelClass.addImportedType("com.fasterxml.jackson.annotation.JsonGetter");
        method.addAnnotation("@JsonGetter(" + getFieldNameConstantName(introspectedColumn.getJavaProperty()) + ")");
    
    
        return true;
    }


    /**
     * If the class is immutable or its instantiation is constructor based, add a @JsonConstructor annotation on 
     * its parametrized constructor, with a @JsonProperty for each of the constructor parameters.
     * @param topLevelClass
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if(introspectedTable.isImmutable() || introspectedTable.isConstructorBased()) {
            Optional<Method> constructorOpt = topLevelClass.getMethods().stream()
                    .filter(Method::isConstructor).findFirst();
            if(constructorOpt.isPresent()) {
                Method constructor = constructorOpt.get();
                topLevelClass.addImportedType("com.fasterxml.jackson.annotation.JsonCreator");
                topLevelClass.addImportedType("com.fasterxml.jackson.annotation.JsonProperty");
                constructor.addAnnotation("@JsonConstructor");
                constructor.getParameters().forEach(parameter -> parameter.addAnnotation("@JsonProperty(" + getFieldNameConstantName(parameter.getName()) + ")"));
            }
                    
        }
        return true;
    }

    
    /**
     * If the class is mutable, generate an annotation @JsonSetter on the currently generated setter.
     * @param method
     * @param topLevelClass
     * @param introspectedColumn
     * @param introspectedTable
     * @param modelClassType
     * @return
     */
    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if(!introspectedTable.isImmutable()) {

            topLevelClass.addImportedType("com.fasterxml.jackson.annotation.JsonSetter");
            method.addAnnotation("@JsonSetter(" + getFieldNameConstantName(introspectedColumn.getJavaProperty()) + ")");

        }

        return true;
    }
    
    
}
