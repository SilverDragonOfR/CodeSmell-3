public static class ShopPolicy extends AbstractResponse<ShopPolicy> implements Node {
    public ShopPolicy() {
    }

    public ShopPolicy(JsonObject fields) throws SchemaViolationError {
        for (Map.Entry<String, JsonElement> field : fields.entrySet()) {
            String key = field.getKey();
            String fieldName = getFieldName(key);
            switch (fieldName) {
                case "body": {
                    responseData.put(key, jsonAsString(field.getValue(), key));

                    break;
                }

                case "handle": {
                    responseData.put(key, jsonAsString(field.getValue(), key));

                    break;
                }

                case "id": {
                    responseData.put(key, new ID(jsonAsString(field.getValue(), key)));

                    break;
                }

                case "title": {
                    responseData.put(key, jsonAsString(field.getValue(), key));

                    break;
                }

                case "url": {
                    responseData.put(key, jsonAsString(field.getValue(), key));

                    break;
                }

                case "__typename": {
                    responseData.put(key, jsonAsString(field.getValue(), key));
                    break;
                }
                default: {
                    throw new SchemaViolationError(this, key, field.getValue());
                }
            }
        }
    }

    public ShopPolicy(ID id) {
        this();
        optimisticData.put("id", id);
    }

    public String getGraphQlTypeName() {
        return "ShopPolicy";
    }

    /**
    * Policy text, maximum size of 64kb.
    */

    public String getBody() {
        return (String) get("body");
    }

    public ShopPolicy setBody(String arg) {
        optimisticData.put(getKey("body"), arg);
        return this;
    }

    /**
    * Policys handle.
    */

    public String getHandle() {
        return (String) get("handle");
    }

    public ShopPolicy setHandle(String arg) {
        optimisticData.put(getKey("handle"), arg);
        return this;
    }

    /**
    * Globally unique identifier.
    */

    public ID getId() {
        return (ID) get("id");
    }

    /**
    * Policys title.
    */

    public String getTitle() {
        return (String) get("title");
    }

    public ShopPolicy setTitle(String arg) {
        optimisticData.put(getKey("title"), arg);
        return this;
    }

    /**
    * Public URL to the policy.
    */

    public String getUrl() {
        return (String) get("url");
    }

    public ShopPolicy setUrl(String arg) {
        optimisticData.put(getKey("url"), arg);
        return this;
    }

    public boolean unwrapsToObject(String key) {
        switch (getFieldName(key)) {
            case "body": return false;

            case "handle": return false;

            case "id": return false;

            case "title": return false;

            case "url": return false;

            default: return false;
        }
    }
}