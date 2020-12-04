package com.zhangqiang.fastdatabase.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.zhangqiang.fastdatabase.annotation.DBEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

@AutoService(value = Processor.class)
public class DBEntityProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportAnnotationTypes = new HashSet<>();
        supportAnnotationTypes.add(DBEntity.class.getCanonicalName());
        return supportAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("================process start!!");

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(DBEntity.class);
        for (Element element : elements) {

            if (element instanceof TypeElement) {

                TypeElement typeElement = (TypeElement) element;


                System.out.println("find target :" + typeElement);
                TypeSpec.Builder daoTypeBuilder = TypeSpec.classBuilder(typeElement.getSimpleName() + "$Dao");
                overrideDaoMethod(daoTypeBuilder,typeElement)
                TypeSpec daoType = daoTypeBuilder
                        .addModifiers(Modifier.PUBLIC)
                        .superclass(getDaoTypeName(ClassName.get(typeElement)))
                        .addMethod(MethodSpec.constructorBuilder().addParameter(String.class, "tableName")
                                .addParameter(getSQLiteOpenHelperTypeName(), "sqLiteOpenHelper")
                                .addModifiers(Modifier.PUBLIC)
                                .addCode("super(tableName,sqLiteOpenHelper);\n")
                                .build())
                        .build();
                JavaFile javaFile = JavaFile.builder(ElementUtils.getPackageName(typeElement), daoType).build();
                try {
                    javaFile.writeTo(mFiler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("================process end!!");
        return true;
    }

    private void overrideDaoMethod(TypeSpec.Builder daoTypeBuilder, TypeElement entityElement) {

        TypeElement daoElement = mElementUtils.getTypeElement("com.zhangqiang.fastdatabase.dao.Dao");
        List<? extends Element> enclosedElements = daoElement.getEnclosedElements();
        for (Element element : enclosedElements) {
            if (element instanceof ExecutableElement) {
                ExecutableElement executableElement = (ExecutableElement) element;
                if (element.getSimpleName().toString().equals("buildPrivateColumnEntries")) {
                    daoTypeBuilder.addMethod(MethodSpec.overriding(executableElement)
                            .addCode(buildCodeBlock_BuildPrivateColumnEntries(entityElement))
                            .build());
                }
            }
        }
    }

    private TypeName getDaoTypeName(TypeName entityTypeName) {
        ClassName className = ClassName.get("com.zhangqiang.fastdatabase.dao", "Dao");
        return ParameterizedTypeName.get(className, entityTypeName);
    }

    private TypeName getSQLiteOpenHelperTypeName() {
        return ClassName.get("android.database.sqlite", "SQLiteOpenHelper");
    }

    private MethodSpec overrideDaoMethod_buildPrivateColumnEntries(TypeElement entityElement) {

        TypeElement daoElement = mElementUtils.getTypeElement("com.zhangqiang.fastdatabase.dao.Dao");
        List<? extends Element> enclosedElements = daoElement.getEnclosedElements();
        for (Element element : enclosedElements) {
            if (element instanceof ExecutableElement) {
                ExecutableElement executableElement = (ExecutableElement) element;
                if (element.getSimpleName().toString().equals("buildPrivateColumnEntries")) {
                    return MethodSpec.overriding(executableElement)
                            .addCode(buildCodeBlock_BuildPrivateColumnEntries(entityElement))
                            .build();
                }
            }
        }
        return null;
    }

    private CodeBlock buildCodeBlock_BuildPrivateColumnEntries(TypeElement entityElement) {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.add("$T<ColumnEntry> entryList = new $T<>();\n",List.class,ArrayList.class);
        List<? extends Element> enclosedElements = entityElement.getEnclosedElements();
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement instanceof VariableElement) {
                VariableElement variableElement = (VariableElement) enclosedElement;
                String varName = variableElement.getSimpleName().toString();

                builder.add("ColumnEntry $N = new ColumnEntry();\n",varName);

                TypeMirror typeMirror = variableElement.asType();
                if (typeMirror.getKind() == TypeKind.INT) {
                    builder.add("$N.setName($S);\n",varName,varName);
                    builder.add("$N.setType($T.INTEGER);\n",varName,getTypeName_ColumnType());
                }
                List<? extends AnnotationMirror> annotationMirrors = variableElement.getAnnotationMirrors();
                for (AnnotationMirror annotationMirror : annotationMirrors) {

                    DeclaredType declaredType = annotationMirror.getAnnotationType();
                    System.out.println("&&&&&&&&&&" + declaredType.asElement().getSimpleName());
                }
                builder.add("entryList.add($N);\n",varName);
            }
        }
        builder.add("return $N;\n","entryList");
        return builder.build();
    }

    private TypeName getTypeName_ColumnEntry(){
        return ClassName.get("com.zhangqiang.fastdatabase.dao","ColumnEntry");
    }

    private TypeName getTypeName_ColumnType(){
        return ClassName.get("com.zhangqiang.fastdatabase.dao","ColumnType");
    }

    private TypeName getTypeName_Index(){
        return ClassName.get("com.zhangqiang.fastdatabase.dao","ColumnType");
    }
}