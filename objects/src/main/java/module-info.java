module exactalgebra.objects.main {
    requires exactalgebra.main;
    requires static exactalgebra.external.main;
    requires org.checkerframework.checker.qual;
    
    exports org.cb2384.exactalgebra.objects;
    exports org.cb2384.exactalgebra.objects.numbers;
    exports org.cb2384.exactalgebra.objects.numbers.rational;
    exports org.cb2384.exactalgebra.objects.numbers.integral;
    exports org.cb2384.exactalgebra.objects.relations;
    exports org.cb2384.exactalgebra.objects.relations.polynomial;
    exports org.cb2384.exactalgebra.objects.pair;
    
    opens org.cb2384.exactalgebra.objects to exactalgebra.text.main;
    opens org.cb2384.exactalgebra.objects.numbers to exactalgebra.text.main;
    opens org.cb2384.exactalgebra.objects.numbers.rational to exactalgebra.text.main;
    opens org.cb2384.exactalgebra.objects.numbers.integral to exactalgebra.text.main;
    opens org.cb2384.exactalgebra.objects.relations to exactalgebra.text.main;
    opens org.cb2384.exactalgebra.objects.relations.polynomial to exactalgebra.text.main;
    opens org.cb2384.exactalgebra.objects.pair to exactalgebra.text.main;
}