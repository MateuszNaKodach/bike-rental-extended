package io.axoniq.demo.bikerental.rental.command;

// TODO(af5): migrate snapshot trigger — AF5 uses SnapshotPolicy on the EventSourcedEntityModule builder.
// Equivalent AF5 config: EventSourcedEntityModule.declarative(String.class, Bike.class)
//     .snapshotPolicy(c -> SnapshotPolicy.afterEvents(10))
//     ...build()
//
// import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
// import org.axonframework.eventsourcing.Snapshotter;
// import org.springframework.stereotype.Component;
//
// @Component("bikeSnapshotDefinition")
// public class BikeSnapshotDefinition extends EventCountSnapshotTriggerDefinition {
//
//     public BikeSnapshotDefinition(Snapshotter snapshotter) {
//         super(snapshotter, 10);
//     }
// }
