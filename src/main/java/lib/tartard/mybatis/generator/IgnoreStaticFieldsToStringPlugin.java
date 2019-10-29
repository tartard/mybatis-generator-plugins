package lib.tartard.mybatis.generator;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.List;
import java.util.Properties;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;

/**
 * Copy of the Mybatis ToStringPlugin with the possibility to ignore static fields in the toString method.
 * 
 * @author Alexandre Hausherr
 */
public class IgnoreStaticFieldsToStringPlugin extends PluginAdapter {

    private boolean useToStringFromRoot;

    private boolean ignoreStaticFields;
    
    private boolean appendHashcode;
    
    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        useToStringFromRoot = isTrue(properties.getProperty("useToStringFromRoot")); //$NON-NLS-1$
        ignoreStaticFields = isTrue(properties.getProperty("ignoreStaticFields"));
        appendHashcode = isTrue(properties.getProperty("appendHashcode"));
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        generateToString(introspectedTable, topLevelClass);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        generateToString(introspectedTable, topLevelClass);
        return true;
    }

    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        generateToString(introspectedTable, topLevelClass);
        return true;
    }

    private void generateToString(IntrospectedTable introspectedTable,
                                  TopLevelClass topLevelClass) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.setName("toString"); //$NON-NLS-1$
        if (introspectedTable.isJava5Targeted()) {
            method.addAnnotation("@Override"); //$NON-NLS-1$
        }

        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3_DSQL) {
            context.getCommentGenerator().addGeneralMethodAnnotation(method,
                    introspectedTable, topLevelClass.getImportedTypes());
        } else {
            context.getCommentGenerator().addGeneralMethodComment(method,
                    introspectedTable);
        }

        method.addBodyLine("StringBuilder sb = new StringBuilder();"); //$NON-NLS-1$
        method.addBodyLine("sb.append(getClass().getSimpleName());"); //$NON-NLS-1$
        method.addBodyLine("sb.append(\" [\");"); //$NON-NLS-1$
        if(appendHashcode) {
            method.addBodyLine("sb.append(\"Hash = \").append(hashCode());"); //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder();
        for (Field field : topLevelClass.getFields()) {
            if(!(ignoreStaticFields && field.isStatic())) {
                String property = field.getName();
                sb.setLength(0);
                sb.append("sb.append(\"").append(", ").append(property) //$NON-NLS-1$ //$NON-NLS-2$
                        .append("=\")").append(".append(").append(property) //$NON-NLS-1$ //$NON-NLS-2$
                        .append(");"); //$NON-NLS-1$
                method.addBodyLine(sb.toString());
            }
        }

        method.addBodyLine("sb.append(\"]\");"); //$NON-NLS-1$
        if (useToStringFromRoot && topLevelClass.getSuperClass() != null) {
            method.addBodyLine("sb.append(\", from super class \");"); //$NON-NLS-1$
            method.addBodyLine("sb.append(super.toString());"); //$NON-NLS-1$
        }
        method.addBodyLine("return sb.toString();"); //$NON-NLS-1$

        topLevelClass.addMethod(method);
    }
}
