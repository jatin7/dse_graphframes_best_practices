// Create Graph
// if dse version < 6.8:
system.graph('northwind').create()

// if dse version = 6.8
//system.graph('northwind').classicEngine().create()

:remote config alias g northwind.g


// Create Properties
schema.propertyKey('customer_id').Text().ifNotExists().create()
schema.propertyKey('company_name').Text().ifNotExists().create()
schema.propertyKey('contact_name').Text().ifNotExists().create()
schema.propertyKey('contact_title').Text().ifNotExists().create()
schema.propertyKey('number').Text().ifNotExists().create()

// Create Vertices
schema.vertexLabel('customer').partitionKey('customer_id').clusteringKey('company_name').properties('contact_name', 'contact_title').ifNotExists().create()
schema.vertexLabel('contact_number').partitionKey('number').ifNotExists().create()

// Create Edges
schema.edgeLabel('contact_at').connection('customer','contact_number').create()
