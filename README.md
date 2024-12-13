<p align="center"><img width="100" src="https://i.ibb.co/BnshRxz/149114972.png" alt="Pano logo"></p>
<p align="center">
  Open-source modular back-end written in Kotlin, backed by @StatuAgency
</p>
<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2025?style=for-the-badge" alt="Maintained">
  <a href="https://github.com/ParsekDev/parsek/blob/main/LICENSE"><img src="https://img.shields.io/github/license/ParsekDev/parsek?style=for-the-badge" alt="License"></a>
</p>

---

## About

Parsek is a core platform designed for back-end applications, allowing developers to focus on their projects without
worrying about speed optimizations, best practices, or scalability.

By offering reusable plugins and rapid development tools, Parsek eliminates the need to write the same feature code for
each project, ensuring faster, more efficient development while maintaining high performance.

#### Project Status

Parsek is used in productions & still under heavy development. There can be breaking changes but we're trying to keep
them as minimum as possible.

#### Requirements

JVM 8+ <br>

## Getting Started

Please follow the documentation at [parsek.dev](https://parsek.dev)!

## Development

Start from here, if you wish to contribute.

#### Prerequisites

JDK 21+ <br>

#### Getting Started

Clone this repository.

```bash
git clone --recursive https://github.com/ParsekDev/parsek.git
cd parsek
```

##### Compile & Run & Debug

```bash
./gradlew run
```

##### Building

If you wish to build locally, you can run this command:

(Production Build)

```bash
./gradlew build
```

(Development Build)

```bash
./gradlew buildDev
```

## Contributing

Merge requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
