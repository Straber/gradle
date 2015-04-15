
Currently much of the software component model is not managed, and so is not visible to rules and cannot take any real
advantage of the features of that rule based configuration offers.

This spec outlines several steps toward a fully managed software component model.

# Feature 1: Key objects in the software model are visible to rules

The goal for this feature is to expose something like the following structure to rules:

    project (aka 'model')
    +-- components
    |   +-- <component-name>
    |       +-- sources
    |       |   +-- <source-set-name>
    |       +-- binaries
    |           +-- <binary-name>
    |               +-- tasks
    |                   +-- <task-name>
    +-- testSuites
        +-- <test-suite-name>
            +-- <same-structure-as-component-above>

For this feature, it is a non-goal to expose any public mechanism for implementing this structure, but this isn't necessarily ruled out either.
The implementation may be fully internal and not extensible (other than adding elements to the various collections above).

At the completion of this feature, it should be possible to:

- Run the `model` report to see something like the above structure.
- Use the rule DSL to configure any of the objects above selected using hard-coded path.

## Implementation

One possible approach is to add each relationship to the graph incrementally, one at a time, fixing the inevitable breakages before moving to the
next.

An implementation could use the internal `MutableModelNode` type to expose each relationship as an edge between two nodes with unmanaged values.
Rules hardcoded in the component base plugins can attach these node and edges as the appropriate model elements become known.

The collections in the graph above are currently represented using `DomainObjectSet`. One possible implementation is to listen for changes to these collections
and attach node and edges as elements are added, bridging the legacy collections into the model graph.
Possibly a better implementation would be to change `CollectionBuilder` into a 'managed map' and use a managed implementation
(not the existing bridged implementation) for these collections instead. This approach would mean some interleaving of this feature and the next.

## Candidate stories

### Make `ComponentSpecContainer` a `CollectionBuilder<ComponentSpec>`

* Remove the ability to view/mutate the component container as a domain object set
* Remove the “bridging” of the component container outside of model space

#### Implementation

* Remove `components` project extension
* Remove model projection that allows access as a domain object set
* Change `ComponentSpecContainer` to `extends CollectionBuilder<ComponentSpec>`
* Add the necessary, possibly adhoc, projection that presents a view that is a ComponentSpecContainer impl

Some, internal only, mechanism will also need to be added to keep the functionality of `ExtensiblePolymorphicDomainObjectContainer`.
That is, the ability to register a kind of factory for new component types.

This story doesn't dictate any kind of implementation for the actual model node other than the ability to project as a `ComponentSpecContainer` (and `CollectionBuilder<T extends ComponentSpec>` sub collections)

#### Test Coverage

- Can view the components container as a `ComponentSpecContainer`
- Can view the components container as a sub set `CollectionBuilder<T>` where T extends `ComponentSpec` (e.g. `PlayCoffeeScriptPlugin.createCoffeeScriptSourceSets`)
- DSL type methods (e.g. Action → Closure) methods can be used on a `ComponentSpecContainer`
- Can continue to register custom component types (i.e. `@ComponentType` rules still work)

Most of this coverage already exists, need to fill in the gaps:

- Build script can:
    - Create a component using a registered component type.
    - Configure components:
        - With given name
        - With given type
        - All components
    - Apply beforeEach/afterEach rules to components.
- Plugin can do the above.
- Reasonable error message when:
    - Attempting to create a component using a type for which there is no implementation.
    - Attempting to create a component using default type.

#### Breaking changes

- Removal of `project.componentSpecs`
- Removal of ability to bind to the component container as `ExtensiblePolymorphicDomainObjectContainer<ComponentSpec>` (if this ever actually worked)
- Removal of `ExtensiblePolymorphicDomainObjectContainer<ComponentSpec>` methods from `ComponentSpecContainer`, addition of collection builder methods
- All configuration done using subject of type `ComponentSpecContainer` is deferred. Used to be eager.

###  `components.«component».source.«sourceSet»` is addressable/visible in rule space

#### Implementation

* Change implementation of `components` node to eagerly create a `source` child node for each element node, with the object returned by `componentSpec.sources`
* `components.«name».source` is projected using the unmanaged projection (i.e. it is opaque)
* Use `.all()` hook of component's source set container to create the child nodes of the `source` node as unmanaged node, based on type of the source set given to the origin create() method

#### Test Coverage

- ~~Can reference `components.«component».source` in a rule (by path, can't bind by type for non top level)~~
- ~~`source` node is displayed for each component in the component container~~
- ~~Can reference `components.«component».source.«source set»` in a rule (by path, can't bind by type for non top level)~~
- ~~Can reference `components.«component».source.«source set»` in a rule as a matching specialisation of `LanguageSourceSet`~~
- ~~`source.«sourceSet»` node is displayed for each source set of each component in the component container~~
- ~~Existing usages of `ProjectSourceSet` continue to work, and corresponding root `sources` node (changing anything here is out of scope)~~

#### Breaking changes

none.

###  `components.«component».binaries.«binary»` is addressable/visible in rule space

#### Implementation

* Change implementation of `components` node to eagerly create a `binaries` child node for each element node, with the object returned by `componentSpec.binaries`
* `components.«name».binaries` is projected using the unmanaged projection (i.e. it is opaque)
* Use `.all()` hook of component's binaries container to create the child nodes of the `binaries` node as unmanaged node, based on the runtime type of the binary

#### Test Coverage

- Can reference `components.«component».binaries` in a rule (by path, can't bind by type for non top level)
- `binaries` node is displayed for each component in the component report
- Can reference `components.«component».binaries.«binary»` in a rule (by path, can't bind by type for non top level)
- Can reference `components.«component».binaries.«binary»` in a rule as a matching specialisation of `BinarySpec`
- `binaries.«binary»` node is displayed for each source set of each component in the component container
- Existing usages of `BinarySpec` continue to work, and corresponding root `binaries` node (changing anything here is out of scope)

#### Breaking changes

none.

###  `components.«component».binaries.«binary».tasks.«task»` is addressable/visible in rule space

#### Implementation

* Change implementation of `«binary»` node to eagerly create a `tasks` child node for each element node, with the object returned by `«binary».task`
* `«binary».tasks` is projected using the unmanaged projection (i.e. it is opaque)
* Use `.all()` hook of binary's task container to create the child nodes of the `tasks` node as unmanaged node, based on the runtime type of the task

#### Test Coverage

- Can reference `components.«component».binaries.«binary».task` in a rule (by path, can't bind by type for non top level)
- `task` node is displayed for each component binary in the component report
- Can reference `components.«component».binaries.«binary».tasks.«task»` in a rule (by path, can't bind by type for non top level)
- Can reference `components.«component».binaries.«binary».tasks.«task»` in a rule as a matching specialisation of `Task`
- `tasks.«task»` node is displayed for each source set of each component in the component container
- Existing usages of `TaskContainer` continue to work, and corresponding root `task` node (changing anything here is out of scope)

#### Breaking changes

none.

### The test suite container has the same level of management/visibility as the general component container

Effectively the same treatment that the component spec container received.

# Feature 2: Configuration of key parts of the software model is deferred until required

This feature changes the software model to introduce 'managed map' types instead of `DomainObjectSet`.

- The property `ComponentSpec.sources`, a collection of `LanguageSourceSet`, should allow any `LanguageSourceSet` type registered using a `@LanguageType` rule to be added.
- The property `ComponentSpec.binaries`, a collection of `BinarySpec`, should allow any `BinarySpec` type registered using a `@BinaryType` rule to be added.
- The property `BinarySpec.tasks`, a collection of `Task`, should allow any `Task` implementation to be added.

At the completion of this feature, it should be possible to write 'before-each', 'after-each', 'all with type' etc rules for the source sets and binaries of a component,
and the tasks of a binary. These rules will be executed as required.

## Implementation

Again, a possible approach is to change each collection, one at a time, and fix the breakages before moving on to the next collection. Breakages are expected
as configuration for the elements of these collections will be deferred, whereas it is currently performed eagerly.

This feature requires a managed map implementation whose values are unmanaged, which means some kind of internal factory will be required for this implementation
(such as `NamedEntityInstantiator`).

Currently the DSL supports nested, eager, configuration of these elements:

    model {
        component {
            someThing {
                sources {
                    cpp { ... }
                }
                binaries {
                    all { ... }
                }
            }
        }
    }

Some replacement for this nesting should be offered, and the configuration deferred.

# Feature 3: Plugin author uses managed types to extend the software model

This feature allows a plugin author to extend certain key types using a managed type:

- `ComponentSpec`
- `LanguageSourceSet`
- `BinarySpec`

It is a non-goal of this feature to add any further capabilities to managed types, or to migrate any of the existing subtypes to managed types.

## Implementation

This feature will require some state for a given object to be unmanaged, possibly attached as node private data, and some state to be managed, backed by
individual nodes.

# Feature 4: Build logic defines tasks for generated source sets and intermediate outputs

This feature generalizes the infrastructure through which build logic defines the tasks that build a binary, and reuses it for generated source sets
and intermediate outputs.

A number of key intermediate outputs will be exposed for their respective binaries:

- Native object files
- JVM class files
- Generated source for play applications

Rules implemented either in a plugin or in the DSL will be able to define the tasks that build a particular binary from its intermediate outputs,
an intermediate output from its input source sets, or a particular source set. Gradle will take care of invoking these rules as required.

Rules will also be able to navigate from the model for a buildable item, such as a binary, intermediate output or source set, to the tasks, for
configuration or reporting.

The `components` report should show details of the intermediate outputs of a binary, the input relationships between the source sets, intermediate outputs and
binaries, plus the task a user would run to build each thing.

It is a non-goal of this feature to provide a public way for a plugin author to define the intermediate outputs for a binary. This is a later feature.

## Implementation

A potential approach:

- Introduce an abstraction that represents a physical thing, where a binary, a source set and intermediate outputs are all physical things.
- Allow the inputs to a physical thing to be modelled. These inputs are also physical things.
- Allow a rule, implemented in a plugin or in the DSL, to define the tasks that build the physical thing.
- Allow navigation from the model for the physical thing to the tasks that are responsible for building it.
- A pre-built physical thing will have no tasks associated with it.

Many of these pieces are already present, and the implementation would formalize these concepts and reuse them.

Currently:

- `BuildableModelElement` represents a physical thing.
- `BinarySpec.tasks` and properties on `BuildableModelElement` represent the tasks that build the thing.
- `BinarySpec.sources` represents the inputs to a binary.
- A `@BinaryTasks` rule defines the tasks that build the binary, as do various methods on `LanguageSourceSet` and `BuildableModelElement`.
- Various types, such as `JvmClasses` and `PublicAssets` represent intermediate outputs.

The implementation would be responsible for invoking the rules when assembling the task graph, so that:

- When a physical thing is to be built, the tasks that build its inputs should be configured.
- When a physical thing is used as input, the tasks that build its inputs, if any, should be determined and attached as dependencies of those tasks
that take the physical thing as input.

# Feature 5: Plugin author uses managed types to define intermediate outputs

This feature allows a plugin author to declare intermediate outputs for custom binaries, using custom types to represent these outputs.

Allow a plugin author to extend any buildable type with a custom managed type. Allow a custom type to declare the inputs for the buildable type in a strongly typed way.
For example, a JVM library binary might declare that it accepts any JVM classpath component as input to build a jar, where the intermediate classes directory is a
kind of JVM classpath component.

## Implementation

One approach is to use annotations to declare the roles of various strongly typed properties of a buildable thing, and use this to infer the inputs
of a buildable thing.

# Feature 6: Build logic defines tasks that run executable things

This feature generalizes the infrastructure through which build logic defines the executable things and how they are to be executed.

The goals for this feature:

- Introduce an abstraction that represents an executable thing, where an installed executable or a test suite variant are executable things.
- Allow a rule to define the tasks that run the executable thing.
- Allow navigation from the model for the executable thing to the tasks that run it.
- Expose an installed native executable and a test suite variant as executable things.

This feature should sync with the plan for play application execution.

## Implementation

The implementation would be responsible for building the executable things, and then configuring and running the appropriate tasks.

The `components` report should show details of the executable things, which as the entry point task to run the thing.

# Feature 7: References between key objects in the software model are visible to rules

The relationships exposed in the first feature represent ownership, where the relationship is one between a parent and child.
This feature exposes other key 'non-ownership' relationships present in the software model.

- A binary has a collection of language source sets that it takes as input. These are not owned by the binary, but form its inputs.
- A test suite component has component under test associated with it.
- The project level binaries collection is a collection of binaries owned by various components.
- The project level sources collection is a collection of language source sets owned by various components.
- The project level task collection is a collection of tasks owned by various binaries, source sets, and other buildable things.

At the completion of this feature, it should be possible to see the above relationships represented in the `model` report.

It is a non-goal of this feature to allow rules to be written to select these objects using their 'non-ownership' paths.

## Implementation

For a binary's input source sets, one option would be to change the behaviour so that a binary receives a copy of its component's source sets. These
copies would then be owned by the binary and can be further customized in the context of the binary.

For a test suite's component under test, one option would be to restructure the relationship, so that test suite(s) become a child of the component under test.