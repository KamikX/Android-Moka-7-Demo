package sk.kamil.morley.plc;


import timber.log.Timber;

public class ReadResult {

    private String error = null;
    private Object result;
    private DataTypeEnum resultType;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public DataTypeEnum getResultType() {
        return resultType;
    }

    public void setResultType(DataTypeEnum resultType) {
        this.resultType = resultType;
    }


    public String getFormattedResult(){

        try {
            if (this.error == null) {

                switch (this.resultType) {

                    case STRING:
                        return (String) this.result;

                    case WORD:
                        return String.valueOf((int)this.result);

                    case REAL:
                        return String.valueOf((float)this.result);

                    default:
                        return (Boolean) this.result ? "true" : "false";
                }
            } else {
                return error;
            }
        } catch (ClassCastException e) {
            Timber.e("Cast exception %s",e.getLocalizedMessage());
        }

        return "something is wrong";
    }

}
