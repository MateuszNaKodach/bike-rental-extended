package io.axoniq.demo.bikerental.rental.command;

import org.springframework.context.annotation.Configuration;

// TODO(af5): uncomment when snapshot migration is done.
// Snapshot policy (was: BikeSnapshotDefinition extends EventCountSnapshotTriggerDefinition, threshold=10)
// AF5 equivalent: configure via EventSourcedEntityModule.declarative(...).snapshotPolicy(c -> SnapshotPolicy.afterEvents(10))
// and provide a SnapshotStore bean (e.g. InMemorySnapshotStore).
//
// import org.axonframework.eventsourcing.snapshot.api.SnapshotPolicy;
// import org.axonframework.eventsourcing.snapshot.inmemory.InMemorySnapshotStore;
// import org.axonframework.eventsourcing.snapshot.store.SnapshotStore;
// import org.springframework.context.annotation.Bean;
//
// @Bean
// SnapshotStore snapshotStore() {
//     return new InMemorySnapshotStore();
// }

@Configuration
class BikeConfiguration {
}
