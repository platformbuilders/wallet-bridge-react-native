require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "wallet-bridge-react-native"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => min_ios_version_supported }
  s.source       = { :git => "https://github.com/platformbuilders/wallet-bridge-react-native.git", :tag => "#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,cpp,swift}"
  s.private_header_files = "ios/**/*.h"

  # ios/AppleWallet/ é Swift, exceto a ponte AppleWallet.h/.mm: o TurboModule
  # implementa a spec ObjC++ gerada pelo codegen (BuildersWalletSpec). O .mm
  # importa o header Swift gerado (wallet_bridge_react_native-Swift.h), o que
  # exige DEFINES_MODULE.
  s.swift_version = "5.0"
  s.pod_target_xcconfig = { "DEFINES_MODULE" => "YES" }

  # PassKit: Apple Pay in-app provisioning. UIKit: apresentação de view
  # controllers e abertura da Wallet. WatchConnectivity: passes do Watch pareado.
  s.frameworks = "PassKit", "UIKit", "WatchConnectivity"

  # Dependencies
  # Sem React-CoreModules: nada no módulo usa, e um pod Swift estático não pode
  # depender de pod sem module map (o CocoaPods recusa a integração).
  s.dependency "React-Core"

  install_modules_dependencies(s)
end
