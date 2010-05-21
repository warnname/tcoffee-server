package converters;

import models.Eval;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;


public class EvalConverter  extends AbstractSingleValueConverter {

        @Override
        public boolean canConvert(Class type) {
                return Eval.class.equals(type);
        }

        @Override
        public Object fromString(String str) {
                return new Eval(str);
        }

}
 