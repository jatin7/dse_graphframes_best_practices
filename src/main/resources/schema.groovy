// Create Graph
system.graph('northwind').create()

:remote config alias g northwind.g

// Create Properties
schema.propertyKey('address_id').Uuid().ifNotExists().create()
schema.propertyKey('address').Text().ifNotExists().create()
schema.propertyKey('city').Text().ifNotExists().create()
schema.propertyKey('region').Text().ifNotExists().create()
schema.propertyKey('year').Int().ifNotExists().create()
schema.propertyKey('postcode').Text().ifNotExists().create()
schema.propertyKey('country').Text().ifNotExists().create()
schema.propertyKey('customer_id').Int().ifNotExists().create()
schema.propertyKey('company_name').Text().ifNotExists().create()
schema.propertyKey('contact_name').Text().ifNotExists().create()
schema.propertyKey('contact_title').Text().ifNotExists().create()
schema.propertyKey('number').Text().ifNotExists().create()
schema.propertyKey('number_type').Text().ifNotExists().create()

// Create Vertices
schema.vertexLabel('customer').partitionKey('customer_id').clusterKey('company_name').properties(contact_name', 'contact_title').ifNotExists().create()
schema.vertexLabel('contact_number').partitionKey('').('number','number_type').ifNotExists().create()

// Create Edges
schema.edgeLabel('contact_at').connection('customer','contact_number').create()

// Create Indices
