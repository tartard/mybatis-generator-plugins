package lib.tartard.mybatis.generator;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;

/**
 * Add validation annotation to fields.
 * Currently added annotations :
 * - \@NotNull to mandatory fields
 * - \@NotBlank to mandatory varchar fields
 * - \@Email to varchar fields which name matches e(-|_)*mail
 * - \@Size to varchar fields limited in size.
 * 
 *
 * @author Alexandre Hausherr
 * @version 1
 */
public class ValidationAnnotationPlugin extends PluginAdapter {
    
    private static final FullyQualifiedJavaType NOTNULL = new FullyQualifiedJavaType("javax.validation.constraints.NotNull");
    private static final FullyQualifiedJavaType NOTBLANK = new FullyQualifiedJavaType("javax.validation.constraints.NotBlank");
    private static final FullyQualifiedJavaType SIZE = new FullyQualifiedJavaType("javax.validation.constraints.Size");
    private static final FullyQualifiedJavaType EMAIL = new FullyQualifiedJavaType("javax.validation.constraints.Email");
    private static final FullyQualifiedJavaType DECIMAL_MAX = new FullyQualifiedJavaType("javax.validation.constraints.DecimalMax");
    private static final FullyQualifiedJavaType DECIMAL_MIN = new FullyQualifiedJavaType("javax.validation.constraints.DecimalMin");
    private static final FullyQualifiedJavaType DIGITS = new FullyQualifiedJavaType("javax.validation.constraints.Digits");
    private static final FullyQualifiedJavaType MAX = new FullyQualifiedJavaType("javax.validation.constraints.Max");
    private static final FullyQualifiedJavaType MIN = new FullyQualifiedJavaType("javax.validation.constraints.Min");
    private static final FullyQualifiedJavaType NULL = new FullyQualifiedJavaType("javax.validation.constraints.Null");
    private static final FullyQualifiedJavaType PATTERN = new FullyQualifiedJavaType("javax.validation.constraints.Pattern");
    private static final FullyQualifiedJavaType VALID = new FullyQualifiedJavaType("javax.validation.Valid");


    private boolean annotateGetters;
    

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        annotateGetters = isTrue(properties.getProperty("annotateGetters"));
    }
    
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
    
    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if(annotateGetters) return true;
        return addAnnotations(field, topLevelClass, introspectedColumn);
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if(!annotateGetters) return true;
        return addAnnotations(method, topLevelClass, introspectedColumn);
    }
    
    private boolean addAnnotations(JavaElement element, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn) {
        if(!introspectedColumn.isNullable() && !introspectedColumn.isIdentity() && !introspectedColumn.isJdbcCharacterColumn()) {
            topLevelClass.addImportedType(NOTNULL);
            element.addAnnotation("@NotNull");
        }

        if(introspectedColumn.isJdbcCharacterColumn()) {
            if(!introspectedColumn.isNullable()) {
                topLevelClass.addImportedType(NOTBLANK);
                element.addAnnotation("@NotBlank");
            }
            topLevelClass.addImportedType(SIZE);
            element.addAnnotation("@Size(max = " + introspectedColumn.getLength() + ")");

            if(introspectedColumn.getActualColumnName().toLowerCase().contains("e(-|_)*mail")) {
                topLevelClass.addImportedType(EMAIL);
                element.addAnnotation("@Email");
            }
        }

        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationAnnotationPlugin that = (ValidationAnnotationPlugin) o;
        return annotateGetters == that.annotateGetters;
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotateGetters);
    }
}