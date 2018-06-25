package com.noober.processor;


import com.noober.api.NeedSave;
import com.noober.api.Supplier;
import com.noober.helper.HelperClass;
import com.noober.helper.HelperSavedValues;
import com.noober.reflect.ParameterizedTypeImpl;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.noober.utils.TypeUtil.ABS_SAVE_HELPER;
import static com.noober.utils.TypeUtil.SAVE_HELPER;


//@AutoService(Processor.class)
public class AutoSaveProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    //存储添加了注解的Activity
    private Map<String, HelperClass> mHelperClassMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        typeUtils = processingEnvironment.getTypeUtils();
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(NeedSave.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(NeedSave.class)) {
            getHelperClass(annotatedElement);
        }

        if (!mHelperClassMap.isEmpty()) {

            TypeSpec.Builder specBuilder = TypeSpec.classBuilder("Table")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addField(ParameterizedTypeImpl.make(HashMap.class, new Type[]{Class.class,
                                    Supplier.class}, null), "__MAP__"
                            , Modifier.STATIC, Modifier.PRIVATE, Modifier.FINAL)
                    .addField(ParameterizedTypeImpl.make(HashMap.class, new Type[]{Class.class, Object.class}, null), "__CACHE__"
                            , Modifier.STATIC, Modifier.PRIVATE, Modifier.FINAL)
                    .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());


            MethodSpec.Builder findSaveHelperBuilder = MethodSpec.methodBuilder("findSaveHelper");
            findSaveHelperBuilder.addParameter(Class.class, "key")
                    .addModifiers(Modifier.STATIC)
                    .returns(Object.class)
                    .addCode("Object obj = __CACHE__.get(key);\n" +
                            "\n" +
                            "        if (obj == null) {\n" +
                            "            Supplier supplier = __MAP__.get(key);\n" +
                            "            if (supplier != null) {\n" +
                            "                obj = supplier.get();\n" +
                            "                __CACHE__.put(key, obj);\n" +
                            "            }\n" +
                            "        }\n" +
                            "\n" +
                            "        return obj;");


            specBuilder.addMethod(findSaveHelperBuilder.build());

            CodeBlock.Builder staticCodeBlockBuilder = CodeBlock.builder();
            staticCodeBlockBuilder.add("__MAP__ = new HashMap<>();\n");
            staticCodeBlockBuilder.add("__CACHE__ = new HashMap<>();\n");

            for (HelperClass helperClass : mHelperClassMap.values()) {
                try {
                    JavaFile javaFile = helperClass.generateCode();
                    if (javaFile != null) {
                        javaFile.writeTo(filer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


                helperClass.addToTable(staticCodeBlockBuilder, "__MAP__");
            }

            specBuilder.addStaticBlock(staticCodeBlockBuilder.build());


            try {
                JavaFile.builder("com.noober.savehelper", specBuilder.build())
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TypeSpec.Builder savehelperSpecBuilder = TypeSpec.classBuilder("SaveHelper")
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());
        savehelperSpecBuilder.superclass(ABS_SAVE_HELPER);

        MethodSpec.Builder findHelperMethodBuilder = MethodSpec.methodBuilder("findHelper")
                .addModifiers(Modifier.PROTECTED)
                .addParameter(Object.class, "save")
                .returns(Object.class);

        findHelperMethodBuilder.addCode("return Table.findSaveHelper(save.getClass());\n");

        savehelperSpecBuilder.addMethod(findHelperMethodBuilder.build());


        savehelperSpecBuilder.addField(SAVE_HELPER, "sInstance", Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .addStaticBlock(CodeBlock.of("sInstance = new SaveHelper();\n"));

        MethodSpec.Builder getInstanceMethodBuilder = MethodSpec.methodBuilder("getInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(SAVE_HELPER)
                .addCode("return sInstance;\n");

        savehelperSpecBuilder.addMethod(getInstanceMethodBuilder.build());

        try {
            JavaFile.builder("com.noober.savehelper", savehelperSpecBuilder.build())
                    .build().writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void getHelperClass(Element element) {
        TypeElement encloseElement = (TypeElement) element.getEnclosingElement();
        String fullClassName = encloseElement.getQualifiedName().toString();
        HelperClass annotatedClass = mHelperClassMap.computeIfAbsent(fullClassName, k -> new HelperClass(encloseElement, elementUtils, messager));
        HelperSavedValues values = new HelperSavedValues(element);
        annotatedClass.addField(values);
    }
}
