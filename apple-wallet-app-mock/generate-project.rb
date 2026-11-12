# frozen_string_literal: true

# ============================================================================
# GERADOR DO PROJETO XCODE — apple-wallet-app-mock
# ============================================================================
# O .xcodeproj é gerado, não versionado (pbxproj é ilegível em diff e conflita
# em merge). Os fontes Swift + Info.plist são a fonte da verdade; este script
# monta o projeto em volta deles.
#
# Usa a gem `xcodeproj`, que já vem com o CocoaPods do example (example/Gemfile).
#
# Uso:
#   BUNDLE_GEMFILE=example/Gemfile bundle exec ruby apple-wallet-app-mock/generate-project.rb
#
# Variáveis (opcionais):
#   APPLE_WALLET_MOCK_BUNDLE_ID   bundle id do app mock (padrão: com.builders.applewalletmock)
#   APPLE_WALLET_MOCK_TARGET_SCHEME  URL scheme do app que a "Wallet" abre
#                                    (padrão: builderswalletexample, o example da lib)

require 'fileutils'
require 'xcodeproj'

ROOT = File.dirname(File.expand_path(__FILE__))
PROJECT_PATH = File.join(ROOT, 'AppleWalletMock.xcodeproj')
SOURCE_DIR = File.join(ROOT, 'AppleWalletMock')
TARGET_NAME = 'AppleWalletMock'
BUNDLE_ID = ENV.fetch('APPLE_WALLET_MOCK_BUNDLE_ID', 'com.builders.applewalletmock')
TARGET_SCHEME = ENV.fetch('APPLE_WALLET_MOCK_TARGET_SCHEME', 'builderswalletexample')
# 16.0 por causa do NavigationStack. Ferramenta de dev, não é publicada.
DEPLOYMENT_TARGET = '16.0'

FileUtils.rm_rf(PROJECT_PATH)

project = Xcodeproj::Project.new(PROJECT_PATH)
target = project.new_target(:application, TARGET_NAME, :ios, DEPLOYMENT_TARGET)
group = project.new_group(TARGET_NAME, SOURCE_DIR)

Dir.glob(File.join(SOURCE_DIR, '*.swift')).sort.each do |path|
  target.add_file_references([group.new_reference(path)])
end
group.new_reference(File.join(SOURCE_DIR, 'Info.plist'))

target.build_configurations.each do |config|
  config.build_settings.merge!(
    'PRODUCT_BUNDLE_IDENTIFIER' => BUNDLE_ID,
    'PRODUCT_NAME' => TARGET_NAME,
    'INFOPLIST_FILE' => 'AppleWalletMock/Info.plist',
    'IPHONEOS_DEPLOYMENT_TARGET' => DEPLOYMENT_TARGET,
    'SWIFT_VERSION' => '5.0',
    'TARGETED_DEVICE_FAMILY' => '1',
    'GENERATE_INFOPLIST_FILE' => 'NO',
    # Lido pelo Info.plist ($(TARGET_URL_SCHEME)) e pelo app em tempo de execução.
    'TARGET_URL_SCHEME' => TARGET_SCHEME,
    # Sem time: roda no Simulator sem assinatura. Para device, defina o time no Xcode.
    'CODE_SIGNING_REQUIRED' => 'NO',
    'CODE_SIGNING_ALLOWED' => 'NO',
    'CODE_SIGN_IDENTITY' => '',
    'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO'
  )
end

project.save

puts "✅ Projeto gerado: #{PROJECT_PATH}"
puts "   Target: #{TARGET_NAME} · Bundle id: #{BUNDLE_ID} · Abre: #{TARGET_SCHEME}://"
