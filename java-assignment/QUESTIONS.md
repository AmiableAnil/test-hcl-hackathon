# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would refactor towards a consistent approach. Currently we have:

1. Panache Active Record Pattern (Store, Product entities extend PanacheEntity)
   - Entity has static methods like Store.findById(), Store.listAll()
   - Simple but couples entity to persistence logic

2. Panache Repository Pattern (FulfillmentRepository extends PanacheRepository)
   - Separate repository class, entity is a plain JPA entity
   - Better separation of concerns

3. Hexagonal/Clean Architecture (Warehouse domain)
   - Domain models separate from JPA entities (Warehouse vs DbWarehouse)
   - Port interfaces (WarehouseStore) with adapter implementations
   - Best testability and flexibility

My recommendation for refactoring:

For a growing codebase, I would standardize on the Repository Pattern (option 2)
as a middle ground:
- It provides good separation of concerns without over-engineering
- Entities remain testable (plain POJOs)
- Easier to mock repositories in unit tests
- Familiar pattern for most Java developers

The Hexagonal approach (option 3) is excellent for complex domains with many
business rules (like Warehouse), but may be overkill for simple CRUD entities
like Product or Store.

I would NOT use Active Record pattern going forward because:
- Static methods are harder to mock in tests
- Tight coupling between entity and persistence
- Violates Single Responsibility Principle
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
API-First (OpenAPI spec generates code) - Used for Warehouse:

Pros:
- Contract-first design ensures API consistency
- Auto-generated DTOs and interfaces reduce boilerplate
- Easy to share spec with frontend/consumers before implementation
- Single source of truth for API documentation
- Changes to API are deliberate and visible in spec diffs

Cons:
- Learning curve for OpenAPI specification syntax
- Generated code can be harder to debug
- Less flexibility for custom response handling
- Build complexity (code generation step)
- IDE may not recognize generated sources without configuration

Code-First (Hand-coded resources) - Used for Product/Store:

Pros:
- Full control over implementation details
- Faster initial development for simple CRUD
- No code generation complexity
- Easier debugging (your code, not generated)
- Can use Quarkus SmallRye OpenAPI to auto-generate spec from code

Cons:
- API documentation can drift from implementation
- More boilerplate code to write
- API changes are scattered across files
- Harder to share contract with consumers upfront

My Choice:

For this project, I would use a HYBRID approach:

1. Use SmallRye OpenAPI annotations on hand-coded resources
   - Write code naturally with @Path, @GET, etc.
   - Add @Operation, @APIResponse annotations for documentation
   - Auto-generate OpenAPI spec from code

2. Reserve API-First for:
   - Public APIs shared with external teams
   - APIs where contract stability is critical
   - Microservices communication contracts

This gives the best of both worlds: developer productivity with hand-coded
resources, plus accurate auto-generated documentation.

I created fulfilment-api-openapi.yaml as a comprehensive reference spec, but the
runtime documentation is generated from the actual code via SmallRye OpenAPI.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
Test Prioritization Strategy (Testing Pyramid):

Priority 1: Integration Tests for REST APIs (Highest ROI)
- Test complete request/response cycle
- Validate HTTP status codes, JSON responses
- Cover happy paths and error cases
- Use @QuarkusTest with RestAssured
- Example: WarehouseResourceImplTest, StoreResourceTest

Priority 2: Unit Tests for Business Logic
- Test use cases and services in isolation
- Mock dependencies (repositories, gateways)
- Focus on complex validation logic
- Example: CreateWarehouseUseCaseTest, ReplaceWarehouseUseCaseTest

Priority 3: Unit Tests for Edge Cases
- Boundary conditions
- Error handling paths
- Exception scenarios

Tests I Would Skip Initially:
- Entity getter/setter tests (trivial)
- Repository tests for simple CRUD (covered by integration tests)
- UI/E2E tests (unless critical user journeys)

Implementation for This Project:

1. Integration Tests (60% of effort)
   - All REST endpoints tested
   - Validation error scenarios
   - 404/400/422 response codes verified
   - Use unique test data (UUID-based) to avoid conflicts

2. Unit Tests (30% of effort)
   - Warehouse use cases (complex business rules)
   - Location gateway
   - Entity mapping logic

3. Coverage Maintenance (10% of effort)
   - JaCoCo integrated with Maven verify phase
   - Minimum 80% coverage threshold
   - Coverage report in CI pipeline

Ensuring Coverage Remains Effective:

1. CI/CD Integration
   - Run tests on every PR
   - Fail build if coverage drops below threshold
   - Coverage diff reported on PRs

2. Test Naming Convention
   - should<ExpectedBehavior>When<Condition>
   - Examples: shouldReturn404WhenWarehouseNotFound

3. Code Review Checklist
   - New features require corresponding tests
   - Bug fixes require regression tests

4. Periodic Review
   - Quarterly review of flaky tests
   - Remove redundant tests
   - Update tests when requirements change

Current Coverage Achieved: 94%
- Exceeds the 80% target
- All critical paths covered
- Business rule validations tested
```