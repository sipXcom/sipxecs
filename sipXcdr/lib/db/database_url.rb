#
# Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
# Contributors retain copyright to elements licensed under a Contributor Agreement.
# Licensed to the User under the LGPL license.
#
##############################################################################

# Holds the params needed to connect to a database
class DatabaseUrl < Struct.new(:database, :port, :host, :adapter, :username, :password)
  
  def initialize(args)
    super()    
    args.each do |field, value| 
        self[field] = value
    end
    self[:database] ||= 'sipxcdr'
    self[:port] ||= 5432 # Default port used by PostgreSQL
    self[:host] ||= 'localhost'
    self[:adapter] ||= 'postgresql'
    if self[:username].nil?
        raise ArgumentError, 'postgres username is required'
    end
    self[:password] ||= ''
  end
  
  def to_dbi
    "dbi:Pg:database=#{database};host=#{host};port=#{port}"
  end
end
