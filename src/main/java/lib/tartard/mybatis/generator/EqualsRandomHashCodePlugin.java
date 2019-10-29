package lib.tartard.mybatis.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.plugins.EqualsHashCodePlugin;

import java.math.BigInteger;
import java.util.*;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;
import static org.mybatis.generator.internal.util.StringUtility.isTrue;

/**
 * Copy of the mybatis EqualsHashCode plugin with the ability of using a different prime number for each generated
 * hashCode method.
 * 
 * @author Alexandre Hausherr
 */
public class EqualsRandomHashCodePlugin extends EqualsHashCodePlugin {
    
    private boolean useEqualsHashCodeFromRoot;

    private BigInteger lastPrime = BigInteger.ZERO;
    
    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        useEqualsHashCodeFromRoot = isTrue(properties.getProperty("useEqualsHashCodeFromRoot")); //$NON-NLS-1$
    }
    
    
    /**
     * Generates a <code>hashCode</code> method that includes all fields.
     *
     * <p>Note that this implementation is based on the eclipse foundation hashCode
     * generator.
     *
     * @param topLevelClass
     *            the class to which the method will be added
     * @param introspectedColumns
     *            column definitions of this class and any superclass of this
     *            class
     * @param introspectedTable
     *            the table corresponding to this class
     */
    protected void generateHashCode(TopLevelClass topLevelClass,
                                    List<IntrospectedColumn> introspectedColumns,
                                    IntrospectedTable introspectedTable) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName("hashCode"); //$NON-NLS-1$
        if (introspectedTable.isJava5Targeted()) {
            method.addAnnotation("@Override"); //$NON-NLS-1$
        }

        if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3_DSQL) {
            context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable,
                    topLevelClass.getImportedTypes());
        } else {
            context.getCommentGenerator().addGeneralMethodComment(method,
                    introspectedTable);
        }

        BigInteger prime = lastPrime.nextProbablePrime();
        if(prime.compareTo(BigInteger.valueOf(Long.valueOf(Integer.MAX_VALUE))) >= 0) {
            throw new RuntimeException("Reached MAX integer for random prime number generation. Cannot continue.");
        }
        method.addBodyLine("final int prime = " + prime.toString() + ";"); //$NON-NLS-1$
        
        lastPrime = prime;
        
        method.addBodyLine("int result = 1;"); //$NON-NLS-1$

        if (useEqualsHashCodeFromRoot && topLevelClass.getSuperClass() != null) {
            method.addBodyLine("result = prime * result + super.hashCode();"); //$NON-NLS-1$
        }

        StringBuilder sb = new StringBuilder();
        boolean hasTemp = false;
        Iterator<IntrospectedColumn> iter = introspectedColumns.iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();

            FullyQualifiedJavaType fqjt = introspectedColumn
                    .getFullyQualifiedJavaType();

            String getterMethod = getGetterMethodName(
                    introspectedColumn.getJavaProperty(), fqjt);

            sb.setLength(0);
            if (fqjt.isPrimitive()) {
                if ("boolean".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb.append("result = prime * result + ("); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("() ? 1231 : 1237);"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                } else if ("byte".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb.append("result = prime * result + "); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("();"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                } else if ("char".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb.append("result = prime * result + "); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("();"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                } else if ("double".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    if (!hasTemp) {
                        method.addBodyLine("long temp;"); //$NON-NLS-1$
                        hasTemp = true;
                    }
                    sb.append("temp = Double.doubleToLongBits("); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("());"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                    method
                            .addBodyLine("result = prime * result + (int) (temp ^ (temp >>> 32));"); //$NON-NLS-1$
                } else if ("float".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb
                            .append("result = prime * result + Float.floatToIntBits("); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("());"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                } else if ("int".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb.append("result = prime * result + "); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("();"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                } else if ("long".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb.append("result = prime * result + (int) ("); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("() ^ ("); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("() >>> 32));"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                } else if ("short".equals(fqjt.getFullyQualifiedName())) { //$NON-NLS-1$
                    sb.append("result = prime * result + "); //$NON-NLS-1$
                    sb.append(getterMethod);
                    sb.append("();"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                } else {
                    // should never happen
                    continue;
                }
            } else if (fqjt.isArray()) {
                // Arrays is already imported by the generateEquals method, we don't need
                // to do it again
                sb.append("result = prime * result + (Arrays.hashCode("); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("()));"); //$NON-NLS-1$
                method.addBodyLine(sb.toString());
            } else {
                sb.append("result = prime * result + (("); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("() == null) ? 0 : "); //$NON-NLS-1$
                sb.append(getterMethod);
                sb.append("().hashCode());"); //$NON-NLS-1$
                method.addBodyLine(sb.toString());
            }
        }

        method.addBodyLine("return result;"); //$NON-NLS-1$

        topLevelClass.addMethod(method);
    }
    
}
