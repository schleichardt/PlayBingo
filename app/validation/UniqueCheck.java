/*
This code is not from Michael Schleichardt.
It was copied on 12-1-2011 from http://www.playframework.org/community/snippets/19 .
This code can be modified.
 */

package validation;

import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.FieldContext;
import net.sf.oval.context.OValContext;
import net.sf.oval.exception.OValException;
import play.db.jpa.JPQL;
import play.db.jpa.Model;

import java.lang.reflect.Field;

public class UniqueCheck extends AbstractAnnotationCheck<Unique> {

    final public static String message = "validation.unique";

    @Override
    public boolean isSatisfied(Object validatedObject, Object value,
                               OValContext context, Validator validator) throws OValException {

        if (value == null) {
            return true;
        }

        Model validatedModel = (Model) validatedObject;

        Field field = field(context);

        String fieldName = field.getName();
        Object fieldValue = fieldValue(field, validatedObject);

        Object[] params = {fieldValue};

        Model model = JPQL.instance.find(validatedModel.getClass().getName(), fieldName + " = ?", params).first();
        if (model == null) {
            return true;
        }

        if (model.id.equals(validatedModel.id)) { // same object
            return true;
        }

        return false;
    }

    private Object fieldValue(Field field, Object validatedObject) {
        try {
            return field.get(validatedObject);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Field field(OValContext context) {
        return ((FieldContext) context).getField();
    }
}