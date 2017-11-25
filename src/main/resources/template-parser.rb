require 'erb'
require 'yaml'

class Parser
  def self.parse(json,template)
    node = YAML.load json
    ERB.new(template,nil,'-').result binding
  end
end

Parser.parse(json,template)