// factory-bot-core: the ORM-agnostic engine.
// Depends only on Datafaker (fake-value generation) — see ADR-0005.

dependencies {
    // Datafaker supplies realistic fake values (names, emails, addresses...).
    api("net.datafaker:datafaker:2.4.2")
}
