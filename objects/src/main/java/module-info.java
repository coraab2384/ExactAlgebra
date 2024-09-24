module org.cb2384.exactalgebra.objects {
    requires org.cb2384.exactalgebra.util;
    requires static com.numericalmethod.suanshu;
    requires org.checkerframework.checker.qual;
    
    exports org.cb2384.exactalgebra.objects;
    exports org.cb2384.exactalgebra.objects.numbers;
    exports org.cb2384.exactalgebra.objects.numbers.rational;
    exports org.cb2384.exactalgebra.objects.numbers.integral;
    exports org.cb2384.exactalgebra.objects.relations;
    exports org.cb2384.exactalgebra.objects.relations.polynomial;
    exports org.cb2384.exactalgebra.objects.pair;
}