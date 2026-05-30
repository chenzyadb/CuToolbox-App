cmake_minimum_required(VERSION 3.18.1)
project("CuToolbox-Native")

set(CMAKE_C_STANDARD 17)
set(CMAKE_C_EXTENSIONS OFF)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_EXTENSIONS OFF)

add_library(cu-native SHARED native-lib.cpp)
target_compile_options(cu-native PRIVATE -O2)
