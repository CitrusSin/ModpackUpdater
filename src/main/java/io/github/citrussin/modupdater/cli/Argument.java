package io.github.citrussin.modupdater.cli;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Argument {
    public String value();
    public boolean isOption() default false;
    public String[] aliases() default {};
    public int index() default 0;
}
